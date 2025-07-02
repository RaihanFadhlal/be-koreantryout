package com.enigma.tekor.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.enigma.tekor.dto.request.TransactionRequest;
import com.enigma.tekor.dto.response.CommonResponse;
import com.enigma.tekor.dto.response.TransactionResponse;
import com.enigma.tekor.service.TransactionService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping("/create")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<CommonResponse<TransactionResponse>> createTransaction(
            @RequestBody TransactionRequest request) {
        TransactionResponse transactionResponse = transactionService.create(request);
        CommonResponse<TransactionResponse> response = CommonResponse.<TransactionResponse>builder()
                .status(HttpStatus.CREATED.getReasonPhrase())
                .message("Transaction created successfully, please proceed to payment.")
                .data(transactionResponse)
                .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/midtrans/webhook")
    public ResponseEntity<Void> handleMidtransNotification(@RequestBody Map<String, Object> notificationPayload) {
        transactionService.handleMidtransNotification(notificationPayload);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/status/{orderId}")
    public ResponseEntity<TransactionResponse> getTransactionStatus(@PathVariable String orderId) {
        TransactionResponse transactionResponse = transactionService.checkTransactionStatus(orderId);
        return ResponseEntity.ok(transactionResponse);
    }
}
