package com.enigma.tekor.service.impl;

import java.util.Map;
import java.util.UUID;

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
import com.enigma.tekor.dto.response.CreateTransactionResponse;
import com.enigma.tekor.dto.response.TransactionResponse;
import com.enigma.tekor.entity.Bundle;
import com.enigma.tekor.entity.TestPackage;
import com.enigma.tekor.entity.Transaction;
import com.enigma.tekor.entity.User;
import com.enigma.tekor.exception.BadRequestException;
import com.enigma.tekor.exception.UserNotFoundException;
import com.enigma.tekor.repository.BundleRepository;
import com.enigma.tekor.repository.TestPackageRepository;
import com.enigma.tekor.repository.TransactionRepository;
import com.enigma.tekor.repository.UserRepository;
import com.enigma.tekor.service.MidtransService;
import com.enigma.tekor.service.TransactionService;
import com.midtrans.httpclient.error.MidtransError;
import com.midtrans.service.MidtransCoreApi;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final TestPackageRepository testPackageRepository;
    private final BundleRepository bundleRepository;
    private final MidtransService midtransService;

    private final MidtransCoreApi midtransCoreApi;
    // private final TestAttemptService testAttemptService;

    private static final Logger log = LoggerFactory.getLogger(TransactionServiceImpl.class);

    @Override
    @Transactional
    public TransactionResponse create(TransactionRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email).orElseThrow(() -> new UserNotFoundException("User not found"));

        if (request.getTestPackageId() == null && request.getBundleId() == null) {
            throw new BadRequestException("Request must include either a test package ID or a bundle ID");
        }

        Transaction transaction = new Transaction();
        transaction.setUser(user);
        transaction.setStatus(TransactionStatus.PENDING);

        String midtransOrderId = "TEKOR-" + UUID.randomUUID().toString();
        transaction.setMidtransOrderId(midtransOrderId);

        if (request.getBundleId() != null) {
            Bundle bundle = bundleRepository.findById(request.getBundleId())
                    .orElseThrow(() -> new RuntimeException("Bundle not found"));
            transaction.setBundle(bundle);
            transaction.setAmount(bundle.getPrice());
        } else {
            TestPackage testPackage = testPackageRepository.findById(request.getTestPackageId())
                    .orElseThrow(() -> new RuntimeException("Test package not found"));
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

        // Cari transaksi berdasarkan midtrans_order_id
        Transaction transaction = transactionRepository.findByMidtransOrderId(orderId)
                .orElseThrow(() -> {
                    // Highlight: (PERBAIKAN) Log error ini karena sangat kritikal.
                    // Ini berarti notifikasi datang untuk order_id yang tidak ada di sistem kita.
                    log.error("CRITICAL: Received notification for non-existent order_id: {}", orderId);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "Transaction with order ID " + orderId + " not found.");
                });

        try {
            // Verifikasi status ke Midtrans (best practice)
            JSONObject transactionResult = midtransCoreApi.checkTransaction(orderId);
            log.info("Verified status for order_id {}: {}", orderId, transactionResult.toString());

            String transactionStatus = transactionResult.getString("transaction_status");
            String fraudStatus = transactionResult.optString("fraud_status");

            // Highlight: (PERBAIKAN) Logika utama untuk memperbarui status transaksi
            // berdasarkan response Midtrans.
            if (transactionStatus.equals("capture")) {
                if (fraudStatus.equals("accept")) {
                    // Untuk pembayaran Kartu Kredit, status "capture" dan fraud "accept" berarti
                    // sukses.
                    transaction.setStatus(TransactionStatus.SUCCESS);
                } else if (fraudStatus.equals("challenge")) {
                    // Status fraud "challenge", perlu review manual di dashboard Midtrans.
                    // Anda bisa membuat status sendiri seperti 'CHALLENGE' atau tetap 'PENDING'.
                    transaction.setStatus(TransactionStatus.PENDING);
                } else {
                    // Status fraud lainnya dianggap gagal.
                    transaction.setStatus(TransactionStatus.FAILED);
                }
            } else if (transactionStatus.equals("settlement")) {
                // Untuk metode pembayaran lain (GoPay, Transfer Bank, dll), status "settlement"
                // berarti sukses.
                transaction.setStatus(TransactionStatus.SUCCESS);
            } else if (transactionStatus.equals("cancel") || transactionStatus.equals("deny")
                    || transactionStatus.equals("expire")) {
                // Status-status ini menunjukkan transaksi gagal atau dibatalkan.
                transaction.setStatus(TransactionStatus.FAILED);
            } else if (transactionStatus.equals("pending")) {
                // Transaksi masih menunggu pembayaran. Tidak perlu mengubah status jika sudah
                // PENDING.
                transaction.setStatus(TransactionStatus.PENDING);
            }

            transactionRepository.save(transaction);
            log.info("Successfully updated transaction status for order_id: {} to {}", orderId,
                    transaction.getStatus());

        } catch (MidtransError e) {
            log.error("Failed to verify transaction status from Midtrans for order_id: " + orderId
                    + ". Error: " + e.getMessage());
            // Berikan response error ke Midtrans agar mereka mencoba mengirim notifikasi
            // lagi nanti
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
                .orderId(transaction.getId().toString())
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
}
