package com.enigma.tekor.service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.enigma.tekor.dto.request.TransactionRequest;
import com.enigma.tekor.dto.response.TransactionDetailResponse;
import com.enigma.tekor.dto.response.TransactionResponse;
import com.enigma.tekor.entity.Transaction;
import com.enigma.tekor.entity.User;

public interface TransactionService {
    TransactionResponse create(TransactionRequest request);
    void handleMidtransNotification(Map<String, Object> payload);
    TransactionResponse checkTransactionStatus(String orderId);
    Transaction getTransactionById(String id);
    List<TransactionResponse> getHistory();
    List<TransactionDetailResponse> getTransactionsByUserId(String userId);
    List<Transaction> getSuccessfulTransactionsByUserId(UUID userId);
    List<Transaction> findAllByUser(User user);
}
