package com.enigma.tekor.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.enigma.tekor.dto.response.CreateTransactionResponse;
import com.enigma.tekor.entity.Transaction;
import com.enigma.tekor.service.MidtransService;
import com.midtrans.httpclient.error.MidtransError;
import com.midtrans.service.MidtransSnapApi;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MidtransServiceImpl implements MidtransService {

    private final MidtransSnapApi midtransSnapApi;
    private static final Logger log = LoggerFactory.getLogger(MidtransServiceImpl.class);

    @Override
    public CreateTransactionResponse createTransaction(Transaction transaction) {

        Map<String, Object> transactionDetails = new HashMap<>();
        transactionDetails.put("order_id", transaction.getMidtransOrderId());
        transactionDetails.put("gross_amount", transaction.getAmount().longValue());

        Map<String, String> itemDetails = new HashMap<>();
        itemDetails.put("id", transaction.getTestPackage() != null ? transaction.getTestPackage().getId().toString()
                : transaction.getBundle().getId().toString());
        itemDetails.put("price", transaction.getAmount().toString());
        itemDetails.put("quantity", "1");
        itemDetails.put("name", transaction.getTestPackage() != null ? transaction.getTestPackage().getName()
                : transaction.getBundle().getName());

        List<Map<String, String>> items = new ArrayList<>();
        items.add(itemDetails);

        Map<String, String> customerDetails = new HashMap<>();
        customerDetails.put("full_name", transaction.getUser().getFullName());
        customerDetails.put("email", transaction.getUser().getEmail());

        Map<String, Object> body = new HashMap<>();
        body.put("transaction_details", transactionDetails);
        body.put("item_details", items);
        body.put("customer_details", customerDetails);

        log.info("Creating Midtrans transaction with payload: {}", body);

        try {
            String redirectUrl = midtransSnapApi.createTransactionRedirectUrl(body);
            log.info("Successfully created Midtrans transaction. Redirect URL: {}", redirectUrl);
            return CreateTransactionResponse.builder().redirectUrl(redirectUrl).build();
        } catch (MidtransError e) {
            log.error("Midtrans API Error. Status Code: {}. Response Body: {}", e.getStatusCode(), e.getResponseBody());
            throw new RuntimeException("Failed to create Midtrans transaction: " + e.getMessage(), e);
        }
    }
}
