package com.enigma.tekor.service.impl;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
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
import com.midtrans.service.MidtransCoreApi;

@Service
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final UserService userService;
    private final TestPackageService testPackageService;
    private final BundleService bundleService;
    private final MidtransService midtransService;
    private final MidtransCoreApi midtransCoreApi;
    private static final Logger log = LoggerFactory.getLogger(TransactionServiceImpl.class);

    public TransactionServiceImpl(
            TransactionRepository transactionRepository,
            @Lazy UserService userService,
            TestPackageService testPackageService,
            BundleService bundleService,
            MidtransService midtransService,
            MidtransCoreApi midtransCoreApi
    ) {
        this.transactionRepository = transactionRepository;
        this.userService = userService;
        this.testPackageService = testPackageService;
        this.bundleService = bundleService;
        this.midtransService = midtransService;
        this.midtransCoreApi = midtransCoreApi;
    }

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
            if (bundle.getDiscountPrice() == null) {
                transaction.setAmount(bundle.getPrice());
            } else {
                transaction.setAmount(bundle.getDiscountPrice());
            }
        } else {
            TestPackage testPackage = testPackageService.getOneById(request.getTestPackageId());
            transaction.setTestPackage(testPackage);
            if (testPackage.getDiscountPrice() == null) {
                transaction.setAmount(testPackage.getPrice());
            } else {
                transaction.setAmount(testPackage.getDiscountPrice());
            }
        }

        transactionRepository.save(transaction);
        log.info("Transaction saved locally with local_id: {} and midtrans_order_id: {}", transaction.getId(),
                transaction.getMidtransOrderId());

        try {
            CreateTransactionResponse midtransResponse = midtransService.createTransaction(transaction);

            return toTransactionResponse(transaction, midtransResponse.getRedirectUrl());
        } catch (Exception e) {
            throw new RuntimeException("Failed to create Midtrans transaction", e);
        }
    }

    @Override
    @Transactional
    public void handleMidtransNotification(Map<String, Object> payload) {
        String orderId = (String) payload.get("order_id");
        log.info("Received Midtrans notification for order_id: {}", orderId);
        log.info("Payload", payload);

        Transaction transaction = transactionRepository.findByMidtransOrderId(orderId)
                .orElseThrow(() -> {
                    log.error("CRITICAL: Received notification for non-existent order_id: {}", orderId);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "Transaction with order ID " + orderId + " not found.");
                });

        String transactionStatus = (String) payload.get("transaction_status");
        String fraudStatus = (String) payload.get("fraud_status");

        if ("capture".equals(transactionStatus)) {
            if ("accept".equals(fraudStatus)) {
                transaction.setStatus(TransactionStatus.SUCCESS);
            } else if ("challenge".equals(fraudStatus)) {
                transaction.setStatus(TransactionStatus.PENDING);
            } else {
                transaction.setStatus(TransactionStatus.FAILED);
            }
        } else if ("settlement".equals(transactionStatus)) {
            transaction.setStatus(TransactionStatus.SUCCESS);
        } else if ("cancel".equals(transactionStatus) || "deny".equals(transactionStatus) || "expire".equals(transactionStatus)) {
            transaction.setStatus(TransactionStatus.FAILED);
        } else if ("pending".equals(transactionStatus)) {
            transaction.setStatus(TransactionStatus.PENDING);
        }

        transactionRepository.save(transaction);
        log.info("Successfully updated transaction status for order_id: {} to {}", orderId,
                transaction.getStatus());
    }

    @Override
    @Transactional(readOnly = true)
    public TransactionResponse checkTransactionStatus(String orderId) {
        Transaction transaction = transactionRepository.findByMidtransOrderId(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Transaction not found"));

        return toTransactionResponse(transaction, getRedirectUrlBasedOnStatus(transaction.getStatus()));
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
                .testPackage(transaction.getTestPackage() != null ? TestPackageResponse.builder()
                        .id(transaction.getTestPackage().getId().toString())
                        .name(transaction.getTestPackage().getName())
                        .description(transaction.getTestPackage().getDescription())
                        .price(transaction.getTestPackage().getPrice() != null
                                ? transaction.getTestPackage().getPrice().doubleValue()
                                : null)
                        .discountPrice(transaction.getTestPackage().getDiscountPrice() != null
                                ? transaction.getTestPackage().getDiscountPrice().doubleValue()
                                : null)
                        .build() : null)
                .bundle(transaction.getBundle() != null ? BundleResponse.builder()
                        .id(transaction.getBundle().getId())
                        .name(transaction.getBundle().getName())
                        .build() : null)
                .build();
    }

    @Override
    public List<Transaction> getSuccessfulTransactionsByUserId(UUID userId) {
        return transactionRepository.findByUserIdAndStatus(
                userId,
                TransactionStatus.SUCCESS);
    }

    @Override
    public List<Transaction> findAllByUser(User user) {
        return transactionRepository.findAllByUser(user);
    }

}