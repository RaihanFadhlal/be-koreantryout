package com.enigma.tekor.service.impl;


import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.enigma.tekor.constant.TestAttemptStatus;
import com.enigma.tekor.dto.request.TestAttemptRequest;
import com.enigma.tekor.dto.response.TestAttemptResponse;
import com.enigma.tekor.entity.TestAttempt;
import com.enigma.tekor.entity.Transaction;
import com.enigma.tekor.repository.TestAttemptRepository;
import com.enigma.tekor.repository.TransactionRepository;
import com.enigma.tekor.service.TestAttemptService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;


@Service
@RequiredArgsConstructor
public class TestAttemptServiceImpl implements TestAttemptService {
    private final TestAttemptRepository testAttemptRepository;
    private final TransactionRepository transactionRepository;
    

     @Override
    @Transactional
    public TestAttemptResponse create(TestAttemptRequest request) {
       
        Transaction transaction = transactionRepository.findById(UUID.fromString(request.getTransactionId()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Transaction not found"));

        if (!"SUCCESS".equals(transaction.getStatus().toString())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Transaction is not successful");
        }

       
        if (testAttemptRepository.existsByTransaction(transaction)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Test attempt already exists for this transaction");
        }

        TestAttempt testAttempt = TestAttempt.builder()
                .transaction(transaction)
                .user(transaction.getUser())
                .testPackage(transaction.getTestPackage())
                .startTime(new Date ())
                .status(TestAttemptStatus.IN_PROGRESS)
                .build();

        testAttemptRepository.saveAndFlush(testAttempt);

        return mapToResponse(testAttempt);
    }

    @Override
    public TestAttemptResponse getById(String id) {
        TestAttempt testAttempt = testAttemptRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Test attempt not found"));
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
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Test attempt not found"));

       
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
