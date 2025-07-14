package com.enigma.tekor.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;
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

import com.enigma.tekor.constant.TestAttemptStatus;
import com.enigma.tekor.constant.TransactionStatus;
import com.enigma.tekor.dto.request.SaveAnswerRequest;
import com.enigma.tekor.entity.TestAttempt;
import com.enigma.tekor.entity.TestPackage;
import com.enigma.tekor.entity.Transaction;
import com.enigma.tekor.entity.User;
import com.enigma.tekor.exception.AccessForbiddenException;
import com.enigma.tekor.repository.TestAttemptRepository;
import com.enigma.tekor.repository.TestPackageRepository;
import com.enigma.tekor.repository.TransactionRepository;
import com.enigma.tekor.repository.UserRepository;
import com.enigma.tekor.security.CustomUserDetails;
import com.enigma.tekor.service.AIEvaluationService;
import com.enigma.tekor.service.UserAnswerService;

import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
class TestAttemptServiceImplTest {

    @Mock
    private TestAttemptRepository testAttemptRepository;

    @Mock
    private TestPackageRepository testPackageRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserAnswerService userAnswerService;

    @Mock
    private AIEvaluationService aiEvaluationService;

    @InjectMocks
    private TestAttemptServiceImpl testAttemptService;

    private User user;
    private TestPackage testPackage;
    private TestAttempt testAttempt;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(UUID.randomUUID());

        testPackage = new TestPackage();
        testPackage.setId(UUID.randomUUID());
        testPackage.setQuestions(Collections.emptyList());

        testAttempt = new TestAttempt();
        testAttempt.setId(UUID.randomUUID());
        testAttempt.setUser(user);
        testAttempt.setTestPackage(testPackage);
        testAttempt.setStatus(TestAttemptStatus.IN_PROGRESS);
        testAttempt.setFinishTime(LocalDateTime.now().plusHours(1));
        testAttempt.setUserAnswers(Collections.emptyList());

        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        CustomUserDetails userDetails = new CustomUserDetails(user);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        SecurityContextHolder.setContext(securityContext);

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
    }

    @Test
    void testCreateTestAttempt_Success() {
        // Given
        Transaction transaction = new Transaction();
        transaction.setTestPackage(testPackage);

        when(testPackageRepository.findById(testPackage.getId())).thenReturn(Optional.of(testPackage));
        when(transactionRepository.findByUserAndStatus(user, TransactionStatus.SUCCESS))
            .thenReturn(Collections.singletonList(transaction));
        when(testAttemptRepository.countByUserAndTestPackage(user, testPackage)).thenReturn(0L);

        // When
        var response = testAttemptService.createTestAttempt(testPackage.getId().toString());

        // Then
        assertNotNull(response);
        assertEquals(user.getId().toString(), response.getUserId());
        verify(testAttemptRepository).save(any());
    }

    @Test
    void testCreateTestAttempt_ThrowsAccessForbidden_WhenNotPurchased() {
        // Given
        when(testPackageRepository.findById(testPackage.getId())).thenReturn(Optional.of(testPackage));
        when(transactionRepository.findByUserAndStatus(user, TransactionStatus.SUCCESS))
            .thenReturn(Collections.emptyList());

        // When & Then
        assertThrows(AccessForbiddenException.class, 
            () -> testAttemptService.createTestAttempt(testPackage.getId().toString()));
    }

    @Test
    void testSaveUserAnswer_Success() {
        // Given
        SaveAnswerRequest request = new SaveAnswerRequest();
        when(testAttemptRepository.findById(testAttempt.getId())).thenReturn(Optional.of(testAttempt));

        // When
        testAttemptService.saveUserAnswer(testAttempt.getId().toString(), request);

        // Then
        verify(userAnswerService).saveAnswer(request, testAttempt);
        verify(testAttemptRepository).save(testAttempt);
    }

    @Test
    void testSubmitAttempt_Success() {
        // Given
        when(testAttemptRepository.findById(testAttempt.getId())).thenReturn(Optional.of(testAttempt));
        when(userAnswerService.calculateScore(testAttempt)).thenReturn(80);
        when(aiEvaluationService.getEvaluation(any())).thenReturn(Mono.just("Good job!"));

        // When
        var response = testAttemptService.submitAttempt(testAttempt.getId().toString());

        // Then
        assertNotNull(response);
        assertEquals(80, response.getScore());
        assertEquals(TestAttemptStatus.COMPLETED, testAttempt.getStatus());
        verify(testAttemptRepository).save(testAttempt);
        verify(aiEvaluationService).getEvaluation(any());
    }
}
