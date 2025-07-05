package com.enigma.tekor.service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.enigma.tekor.constant.TestAttemptStatus;
import com.enigma.tekor.dto.request.TransactionRequest;
import com.enigma.tekor.dto.response.TransactionDetailResponse;
import com.enigma.tekor.dto.response.TransactionResponse;
import com.enigma.tekor.entity.TestAttempt;
import com.enigma.tekor.entity.Transaction;

public interface TransactionService {
    TransactionResponse create(TransactionRequest request);
    void handleMidtransNotification(Map<String, Object> payload);
    TransactionResponse checkTransactionStatus(String orderId);
    Transaction getTransactionById(String id);

    List<TransactionResponse> getHistory();
    
    List<TransactionDetailResponse> getTransactionsByUserId(String userId);

    List<Transaction> getSuccessfulTransactionsByUserId(UUID userId);
}
