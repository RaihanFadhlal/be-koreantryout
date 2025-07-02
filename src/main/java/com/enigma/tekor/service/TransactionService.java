package com.enigma.tekor.service;

import java.util.Map;

import com.enigma.tekor.dto.request.TransactionRequest;
import com.enigma.tekor.dto.response.TransactionResponse;
import com.enigma.tekor.entity.Transaction;

public interface TransactionService {
    TransactionResponse create(TransactionRequest request);
    void handleMidtransNotification(Map<String, Object> payload);
    TransactionResponse checkTransactionStatus(String orderId);
    Transaction getTransactionById(String id);
}
