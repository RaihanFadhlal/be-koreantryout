package com.enigma.tekor.service.impl;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import com.enigma.tekor.constant.TransactionStatus;
import com.enigma.tekor.dto.request.TransactionRequest;
import com.enigma.tekor.dto.response.CreateTransactionResponse;
import com.enigma.tekor.dto.response.TransactionResponse;
import com.enigma.tekor.entity.TestPackage;
import com.enigma.tekor.entity.Transaction;
import com.enigma.tekor.entity.User;
import com.enigma.tekor.repository.TransactionRepository;
import com.enigma.tekor.service.BundleService;
import com.enigma.tekor.service.MidtransService;
import com.enigma.tekor.service.TestPackageService;
import com.enigma.tekor.service.UserService;

@ExtendWith(MockitoExtension.class)
class TransactionServiceImplTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private UserService userService;

    @Mock
    private TestPackageService testPackageService;

    @Mock
    private BundleService bundleService;

    @Mock
    private MidtransService midtransService;

    @InjectMocks
    private TransactionServiceImpl transactionService;

    @BeforeEach
    void setUp() {
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void testCreateTransaction_Success() throws Exception {
        // Given
        TransactionRequest request = new TransactionRequest();
        request.setTestPackageId(UUID.randomUUID());

        User user = new User();
        user.setEmail("test@example.com");

        TestPackage testPackage = new TestPackage();
        testPackage.setPrice(new BigDecimal(10000));

        CreateTransactionResponse midtransResponse = new CreateTransactionResponse();
        midtransResponse.setRedirectUrl("http://example.com/payment");

        when(SecurityContextHolder.getContext().getAuthentication().getName()).thenReturn("test@example.com");
        when(userService.getByEmail("test@example.com")).thenReturn(user);
        when(testPackageService.getOneById(any())).thenReturn(testPackage);
        when(midtransService.createTransaction(any())).thenReturn(midtransResponse);

        // When
        var transactionResponse = transactionService.create(request);

        // Then
        assertNotNull(transactionResponse);
        assertEquals("http://example.com/payment", transactionResponse.getRedirectUrl());
        verify(transactionRepository, times(1)).save(any());
    }

    @Test
    void testHandleMidtransNotification_Success() {
        // Given
        Map<String, Object> payload = new HashMap<>();
        payload.put("order_id", "order-123");
        payload.put("transaction_status", "settlement");
        payload.put("fraud_status", "accept");

        Transaction transaction = new Transaction();
        transaction.setStatus(TransactionStatus.PENDING);

        when(transactionRepository.findByMidtransOrderId("order-123")).thenReturn(Optional.of(transaction));

        // When
        transactionService.handleMidtransNotification(payload);

        // Then
        assertEquals(TransactionStatus.SUCCESS, transaction.getStatus());
        verify(transactionRepository).save(transaction);
    }

    @Test
    void testCheckTransactionStatus_Found() {
        // Given
        Transaction transaction = new Transaction();
        transaction.setStatus(TransactionStatus.SUCCESS);
        transaction.setMidtransOrderId("order-123");

        when(transactionRepository.findByMidtransOrderId("order-123")).thenReturn(Optional.of(transaction));

        // When
        TransactionResponse response = transactionService.checkTransactionStatus("order-123");

        // Then
        assertNotNull(response);
        assertEquals(TransactionStatus.SUCCESS.name(), response.getTransactionStatus());
    }
}
