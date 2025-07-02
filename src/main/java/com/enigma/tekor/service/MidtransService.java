package com.enigma.tekor.service;

import com.enigma.tekor.dto.response.CreateTransactionResponse;
import com.enigma.tekor.entity.Transaction;

public interface MidtransService {
    CreateTransactionResponse createTransaction(Transaction transaction);
}
