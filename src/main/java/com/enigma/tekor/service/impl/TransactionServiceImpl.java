package com.enigma.tekor.service.impl;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.enigma.tekor.constant.TransactionStatus;
import com.enigma.tekor.dto.request.TransactionRequest;
import com.enigma.tekor.dto.response.BundleResponse;
import com.enigma.tekor.dto.response.CreateTransactionResponse;
import com.enigma.tekor.dto.response.TestPackageResponse;
import com.enigma.tekor.dto.response.TransactionDetailResponse;
import com.enigma.tekor.dto.response.TransactionResponse;
import com.enigma.tekor.entity.Bundle;
import com.enigma.tekor.entity.TestPackage;
import com.enigma.tekor.entity.Transaction;
import com.enigma.tekor.entity.User;
import com.enigma.tekor.exception.BadRequestException;
import com.enigma.tekor.exception.NotFoundException;
import com.enigma.tekor.repository.TransactionRepository;
import com.enigma.tekor.service.BundleService;
import com.enigma.tekor.service.MidtransService;
import com.enigma.tekor.service.TestPackageService;
import com.enigma.tekor.service.TransactionService;
import com.enigma.tekor.service.UserService;
import com.midtrans.httpclient.error.MidtransError;
import com.midtrans.service.MidtransCoreApi;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final UserService userService;
    private final TestPackageService testPackageService;
    private final BundleService bundleService;
    private final MidtransService midtransService;

    private final MidtransCoreApi midtransCoreApi;

    private static final Logger log = LoggerFactory.getLogger(TransactionServiceImpl.class);

    @Override
    @Transactional
    public TransactionResponse create(TransactionRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.getByEmail(email);

        if (request.getTestPackageId() == null && request.getBundleId() == null) {
            throw new BadRequestException("Request must include either a test package ID or a bundle ID");
        }

        Transaction transaction = new Transaction();
        transaction.setUser(user);
        transaction.setStatus(TransactionStatus.PENDING);

        String midtransOrderId = "TEKOR-" + UUID.randomUUID().toString();
        transaction.setMidtransOrderId(midtransOrderId);

        if (request.getBundleId() != null) {
            Bundle bundle = bundleService.getBundleById(request.getBundleId());
            transaction.setBundle(bundle);
            transaction.setAmount(bundle.getPrice());
        } else {
            TestPackage testPackage = testPackageService.getOneById(request.getTestPackageId());
            transaction.setTestPackage(testPackage);
            transaction.setAmount(testPackage.getPrice());
        }

        transactionRepository.save(transaction);
        log.info("Transaction saved locally with local_id: {} and midtrans_order_id: {}", transaction.getId(),
                transaction.getMidtransOrderId());

        try {
            CreateTransactionResponse midtransResponse = midtransService.createTransaction(transaction);

            return TransactionResponse.builder()
                    .orderId(transaction.getMidtransOrderId())
                    .redirectUrl(midtransResponse.getRedirectUrl())
                    .transactionStatus(transaction.getStatus().name())
                    .build();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create Midtrans transaction", e);
        }
    }

    @Override
    @Transactional
    public void handleMidtransNotification(Map<String, Object> payload) {
        String orderId = (String) payload.get("order_id");
        log.info("Received Midtrans notification for order_id: {}", orderId);

        Transaction transaction = transactionRepository.findByMidtransOrderId(orderId)
                .orElseThrow(() -> {
                    log.error("CRITICAL: Received notification for non-existent order_id: {}", orderId);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "Transaction with order ID " + orderId + " not found.");
                });

        try {
            JSONObject transactionResult = midtransCoreApi.checkTransaction(orderId);
            log.info("Verified status for order_id {}: {}", orderId, transactionResult.toString());

            String transactionStatus = transactionResult.getString("transaction_status");
            String fraudStatus = transactionResult.optString("fraud_status");

            if (transactionStatus.equals("capture")) {
                if (fraudStatus.equals("accept")) {

                    transaction.setStatus(TransactionStatus.SUCCESS);
                } else if (fraudStatus.equals("challenge")) {
                    transaction.setStatus(TransactionStatus.PENDING);
                } else {
                    transaction.setStatus(TransactionStatus.FAILED);
                }
            } else if (transactionStatus.equals("settlement")) {
                transaction.setStatus(TransactionStatus.SUCCESS);
            } else if (transactionStatus.equals("cancel") || transactionStatus.equals("deny")
                    || transactionStatus.equals("expire")) {
                transaction.setStatus(TransactionStatus.FAILED);
            } else if (transactionStatus.equals("pending")) {
                transaction.setStatus(TransactionStatus.PENDING);
            }

            transactionRepository.save(transaction);
            log.info("Successfully updated transaction status for order_id: {} to {}", orderId,
                    transaction.getStatus());

        } catch (MidtransError e) {
            log.error("Failed to verify transaction status from Midtrans for order_id: " + orderId
                    + ". Error: " + e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to verify status with Midtrans");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public TransactionResponse checkTransactionStatus(String orderId) {
        Transaction transaction = transactionRepository.findByMidtransOrderId(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Transaction not found"));

        return TransactionResponse.builder()
                .orderId(transaction.getMidtransOrderId())
                .transactionStatus(transaction.getStatus().name())
                .redirectUrl(getRedirectUrlBasedOnStatus(transaction.getStatus()))
                .build();
    }

    private String getRedirectUrlBasedOnStatus(TransactionStatus status) {
        if (status == TransactionStatus.SUCCESS) {
            return "/payment-success";
        } else if (status == TransactionStatus.FAILED) {
            return "/payment-failed";
        }
        return "/payment-pending";
    }

    @Override
    public Transaction getTransactionById(String id) {
        return transactionRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new NotFoundException("Transaction not found"));
    }

    @Override
    public List<TransactionResponse> getHistory() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.getByEmail(email);

        List<Transaction> transactions = transactionRepository.findByUserWithDetails(user);

        return transactions.stream()
                .map(this::toTransactionResponse)
                .collect(Collectors.toList());
    }

    private TransactionResponse toTransactionResponse(Transaction transaction) {
        return toTransactionResponse(transaction, null);
    }

    private TransactionResponse toTransactionResponse(Transaction transaction, String redirectUrl) {
        String packageName = Optional.ofNullable(transaction.getTestPackage())
                                    .map(TestPackage::getName)
                                    .orElse(null);

        String bundleName = Optional.ofNullable(transaction.getBundle())
                                    .map(Bundle::getName)
                                    .orElse(null);

        return TransactionResponse.builder()
                .orderId(transaction.getMidtransOrderId())
                .transactionStatus(transaction.getStatus().name())
                .amount(transaction.getAmount())
                .packageName(packageName)
                .bundleName(bundleName)
                .createdAt(transaction.getCreatedAt())
                .redirectUrl(redirectUrl)
                .build();
    }


    @Override
    @Transactional(readOnly = true)
    public List<TransactionDetailResponse> getTransactionsByUserId(String userId) {
    
     String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
     User currentUser = userService.getByEmail(currentUserEmail);
    
     if (!currentUser.getId().toString().equals(userId) && 
         !currentUser.getRole().getName().equals("ROLE_ADMIN")) {
         throw new ResponseStatusException(HttpStatus.FORBIDDEN, 
             "You don't have access to this transaction");
     }
     
     
     return transactionRepository.findByUserId(UUID.fromString(userId)).stream()
             .map(this::mapToTransactionDetailResponse)
             .collect(Collectors.toList());
 }
 
    private TransactionDetailResponse mapToTransactionDetailResponse(Transaction transaction) {
     return TransactionDetailResponse.builder()
             .id(transaction.getId().toString())
             .midtransOrderId(transaction.getMidtransOrderId())
             .amount(transaction.getAmount())
             .status(transaction.getStatus().name())
             .createdAt(transaction.getCreatedAt())
             .testPackage(transaction.getTestPackage() != null ? 
                    TestPackageResponse.builder()
                            .id(transaction.getTestPackage().getId().toString())
                            .name(transaction.getTestPackage().getName())
                            .description(transaction.getTestPackage().getDescription()) 
                            .price(transaction.getTestPackage().getPrice() != null ? 
                                  transaction.getTestPackage().getPrice().doubleValue() : null) 
                            .discountPrice(transaction.getTestPackage().getDiscountPrice() != null ? 
                                  transaction.getTestPackage().getDiscountPrice().doubleValue() : null) 
                            .build() : null)
             .bundle(transaction.getBundle() != null ? 
                     BundleResponse.builder()
                             .id(transaction.getBundle().getId())
                             .name(transaction.getBundle().getName())
                             .build() : null)
             .build();
 }

}