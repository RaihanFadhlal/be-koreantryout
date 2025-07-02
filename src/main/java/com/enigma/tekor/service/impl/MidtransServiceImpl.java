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
    // Highlight: Menambahkan logger untuk debugging
    private static final Logger log = LoggerFactory.getLogger(MidtransServiceImpl.class);

    @Override
    public CreateTransactionResponse createTransaction(Transaction transaction) {

        // 1. Siapkan transaction_details
        Map<String, Object> transactionDetails = new HashMap<>();
        transactionDetails.put("order_id", transaction.getMidtransOrderId());
        // Highlight: Pastikan amount adalah Long atau Integer, bukan double/float.
        // Midtrans mengharapkan angka bulat untuk gross_amount.
        transactionDetails.put("gross_amount", transaction.getAmount().longValue());

        // 2. Siapkan item_details (Sangat penting untuk beberapa metode pembayaran)
        Map<String, String> itemDetails = new HashMap<>();
        itemDetails.put("id", transaction.getTestPackage() != null ? transaction.getTestPackage().getId().toString()
                : transaction.getBundle().getId().toString());
        itemDetails.put("price", transaction.getAmount().toString());
        itemDetails.put("quantity", "1");
        itemDetails.put("name", transaction.getTestPackage() != null ? transaction.getTestPackage().getName()
                : transaction.getBundle().getName());

        List<Map<String, String>> items = new ArrayList<>();
        items.add(itemDetails);

        // 3. Siapkan customer_details
        Map<String, String> customerDetails = new HashMap<>();
        customerDetails.put("full_name", transaction.getUser().getFullName());
        customerDetails.put("email", transaction.getUser().getEmail());

        // 4. Gabungkan semua parameter ke dalam body request
        Map<String, Object> body = new HashMap<>();
        body.put("transaction_details", transactionDetails);
        body.put("item_details", items);
        body.put("customer_details", customerDetails);

        // Highlight: (BEST PRACTICE) Tambahkan logging untuk melihat payload yang
        // dikirim.
        // Ini sangat membantu saat debugging.
        log.info("Creating Midtrans transaction with payload: {}", body);

        try {
            // Menggunakan createTransactionRedirectUrl karena Anda ingin mengarahkan
            // pengguna ke halaman pembayaran
            String redirectUrl = midtransSnapApi.createTransactionRedirectUrl(body);
            log.info("Successfully created Midtrans transaction. Redirect URL: {}", redirectUrl);
            return CreateTransactionResponse.builder().redirectUrl(redirectUrl).build();
        } catch (MidtransError e) {
            // Highlight: Log error dari Midtrans dengan lebih detail.
            log.error("Midtrans API Error. Status Code: {}. Response Body: {}", e.getStatusCode(), e.getResponseBody());
            throw new RuntimeException("Failed to create Midtrans transaction: " + e.getMessage(), e);
        }
    }
}
