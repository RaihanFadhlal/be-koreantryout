package com.enigma.tekor.service.impl;

import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.enigma.tekor.constant.TestAttemptStatus;
import com.enigma.tekor.dto.request.SaveAnswerRequest;
import com.enigma.tekor.dto.request.TestAttemptRequest;
import com.enigma.tekor.dto.response.TestAttemptResponse;
import com.enigma.tekor.entity.TestAttempt;
import com.enigma.tekor.entity.Transaction;
import com.enigma.tekor.entity.User;
import com.enigma.tekor.exception.BadRequestException;
import com.enigma.tekor.exception.ConflictException;
import com.enigma.tekor.exception.NotFoundException;
import com.enigma.tekor.repository.TestAttemptRepository;
import com.enigma.tekor.service.TestAttemptService;
import com.enigma.tekor.service.TransactionService;
import com.enigma.tekor.service.UserAnswerService;
import com.enigma.tekor.service.UserService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TestAttemptServiceImpl implements TestAttemptService {
    private final TestAttemptRepository testAttemptRepository;
    private final TransactionService transactionService;
    private final UserService userService;
    private final UserAnswerService userAnswerService;

    @Override
    @Transactional
    public TestAttemptResponse create(TestAttemptRequest request) {

        Transaction transaction = transactionService.getTransactionById(request.getTransactionId());

        if (!"SUCCESS".equals(transaction.getStatus().toString())) {
            throw new BadRequestException("Transaction is not successful");
        }

        if (testAttemptRepository.existsByTransaction(transaction)) {
            throw new ConflictException("Test attempt already exists for this transaction");
        }

        TestAttempt testAttempt = TestAttempt.builder()
                .transaction(transaction)
                .user(transaction.getUser())
                .testPackage(transaction.getTestPackage())
                .startTime(new Date())
                .status(TestAttemptStatus.IN_PROGRESS)
                .build();

        testAttemptRepository.saveAndFlush(testAttempt);

        return mapToResponse(testAttempt);
    }

    @Override
    public TestAttemptResponse getById(String id) {
        TestAttempt testAttempt = testAttemptRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new NotFoundException("Test attempt not found"));
        return mapToResponse(testAttempt);
    }

    @Override
    public List<TestAttemptResponse> getAll() {
        return testAttemptRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public TestAttemptResponse update(TestAttemptRequest request) {
        TestAttempt testAttempt = testAttemptRepository.findById(UUID.fromString(request.getId()))
                .orElseThrow(() -> new NotFoundException("Test attempt not found"));

        if (request.getStatus() == TestAttemptStatus.COMPLETED && testAttempt.getEndTime() == null) {
            testAttempt.setEndTime(new Date());
        }

        if (request.getScore() != null) {
            testAttempt.setScore(request.getScore());
        }
        if (request.getStatus() != null) {
            testAttempt.setStatus(request.getStatus());
        }
        if (request.getAiEvaluationResult() != null) {
            testAttempt.setAiEvaluationResult(request.getAiEvaluationResult());
        }

        testAttemptRepository.save(testAttempt);
        return mapToResponse(testAttempt);
    }

    @Transactional
    @Override
    public void saveUserAnswer(String attemptId, SaveAnswerRequest request) {
        TestAttempt attempt = testAttemptRepository.findById(UUID.fromString(attemptId))
                .orElseThrow(() -> new NotFoundException("Test attempt not found"));

        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userService.getByEmail(currentUsername);

        if (!attempt.getUser().getId().equals(currentUser.getId())) {
            throw new BadRequestException("You are not authorized to access this attempt");
        }

        if (!attempt.getStatus().equals(TestAttemptStatus.IN_PROGRESS)) {
            throw new BadRequestException("This test is no longer in progress");
        }
        userAnswerService.saveAnswer(request, attempt);

        attempt.setRemainingDuration(request.getRemainingTimeInSeconds());
        testAttemptRepository.save(attempt);
    }

    @Transactional
    @Override
    public void submitAttempt(String attemptId) {
        TestAttempt attempt = testAttemptRepository.findById(UUID.fromString(attemptId))
                .orElseThrow(() -> new NotFoundException("Test attempt not found"));

        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userService.getByEmail(currentUsername);

        if (!attempt.getUser().getId().equals(currentUser.getId())) {
            throw new BadRequestException("You are not authorized to submit this attempt");
        }

        if (!attempt.getStatus().equals(TestAttemptStatus.IN_PROGRESS)) {
            throw new BadRequestException("This test is not in progress");
        }

        attempt.setEndTime(new Date());
        attempt.setStatus(TestAttemptStatus.COMPLETED);
        testAttemptRepository.save(attempt);
    }

    @Override
    public TestAttempt getTestAttemptById(String id) {
        return testAttemptRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new NotFoundException("Test attempt with ID: " + id + " not found"));
    }

    private TestAttemptResponse mapToResponse(TestAttempt testAttempt) {
        return TestAttemptResponse.builder()
                .id(testAttempt.getId().toString())
                .transactionId(testAttempt.getTransaction().getId().toString())
                .userId(testAttempt.getUser().getId().toString())
                .packageId(testAttempt.getTestPackage().getId().toString())
                .startTime(testAttempt.getStartTime())
                .endTime(testAttempt.getEndTime())
                .score(testAttempt.getScore())
                .status(testAttempt.getStatus())
                .aiEvaluationResult(testAttempt.getAiEvaluationResult())
                .createdAt(testAttempt.getCreatedAt())
                .build();
    }
}
