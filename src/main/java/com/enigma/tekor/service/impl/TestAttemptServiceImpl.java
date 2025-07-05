package com.enigma.tekor.service.impl;

import com.enigma.tekor.constant.TestAttemptStatus;
import com.enigma.tekor.dto.request.SaveAnswerRequest;
import com.enigma.tekor.dto.request.TestAttemptRequest;
import com.enigma.tekor.dto.response.InProgressAttempt;
import com.enigma.tekor.dto.response.ReadyTestPackage;
import com.enigma.tekor.dto.response.TestAttemptResponse;
import com.enigma.tekor.dto.response.TestPackageResponse;
import com.enigma.tekor.dto.response.UserTestAttemptResponse;
import com.enigma.tekor.entity.TestAttempt;
import com.enigma.tekor.entity.TestPackage;
import com.enigma.tekor.entity.Transaction;
import com.enigma.tekor.dto.request.AIEvaluationRequest;
import com.enigma.tekor.dto.request.UserAnswerEvaluationRequest;
import com.enigma.tekor.entity.User;
import com.enigma.tekor.entity.UserAnswer;
import com.enigma.tekor.exception.BadRequestException;
import com.enigma.tekor.exception.ConflictException;
import com.enigma.tekor.exception.NotFoundException;
import com.enigma.tekor.repository.TestAttemptRepository;
import com.enigma.tekor.service.AIEvaluationService;
import com.enigma.tekor.service.TestAttemptService;
import com.enigma.tekor.service.TransactionService;
import com.enigma.tekor.service.QuestionService;
import com.enigma.tekor.service.UserAnswerService;
import com.enigma.tekor.service.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TestAttemptServiceImpl implements TestAttemptService {
    private final TestAttemptRepository testAttemptRepository;
    private final TransactionService transactionService;
    private final UserService userService;
    private final UserAnswerService userAnswerService;
    private final QuestionService questionService;
    private final AIEvaluationService aiEvaluationService;

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

        Integer score = userAnswerService.calculateScore(attempt);
        attempt.setScore(Float.valueOf(score));

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

        Integer score = userAnswerService.calculateScore(attempt);

        attempt.setEndTime(new Date());
        attempt.setStatus(TestAttemptStatus.COMPLETED);
        attempt.setScore(Float.valueOf(score));
        testAttemptRepository.save(attempt);

        List<UserAnswerEvaluationRequest> userAnswerRequests = attempt.getUserAnswers().stream()
        .map(this::mapUserAnswerToEvaluationRequest)
        .collect(Collectors.toList());

        AIEvaluationRequest aiRequest = AIEvaluationRequest.builder()
                .testAttemptId(attempt.getId())
                .score(attempt.getScore())
                .userAnswers(userAnswerRequests)
                .build();

        aiEvaluationService.getEvaluation(aiRequest)
                .subscribe(evaluation -> {
                    attempt.setAiEvaluationResult(evaluation);
                    testAttemptRepository.save(attempt);
                });
    }

    private UserAnswerEvaluationRequest mapUserAnswerToEvaluationRequest(UserAnswer userAnswer) {
        return UserAnswerEvaluationRequest.builder()
                .questionType(userAnswer.getQuestion().getQuestionType().name())
                .isCorrect(userAnswer.getIsCorrect())
                .build();
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


    @Override
    public UserTestAttemptResponse getUserTestAttempt(String userId) {
        UUID userUuid = UUID.fromString(userId);

        List<Transaction> successfulTransactions = transactionService.getSuccessfulTransactionsByUserId(userUuid);

        List<TestAttempt> inProgressAttempts = testAttemptRepository.findByUserIdAndStatus(
                userUuid,
                TestAttemptStatus.IN_PROGRESS);


        return UserTestAttemptResponse.builder()
                .readyToStart(mapReadyPackages(successfulTransactions))
                .inProgress(mapInProgressAttempts(inProgressAttempts))
                .build();
    }

    private List<ReadyTestPackage> mapReadyPackages(List<Transaction> transactions) {
        return transactions.stream()
                .map(this::mapToReadyTestPackage)
                .collect(Collectors.toList());
    }

    private List<InProgressAttempt> mapInProgressAttempts(List<TestAttempt> attempts) {
        return attempts.stream()
                .map(this::mapToInProgressAttempt)
                .collect(Collectors.toList());
    }

    private ReadyTestPackage mapToReadyTestPackage(Transaction transaction) {
        return ReadyTestPackage.builder()
                .transactionId(transaction.getId().toString())
                .testPackage(mapToTestPackageResponse(transaction.getTestPackage()))
                .purchaseDate(convertToDate(transaction.getCreatedAt()))
                .build();
    }

    private InProgressAttempt mapToInProgressAttempt(TestAttempt attempt) {
        return InProgressAttempt.builder()
                .attemptId(attempt.getId().toString())
                .testPackage(mapToTestPackageResponse(attempt.getTestPackage()))
                .startTime(attempt.getStartTime())
                .remainingDuration(attempt.getRemainingDuration())
                .build();
    }

    private TestPackageResponse mapToTestPackageResponse(TestPackage testPackage) {
        if (testPackage == null) {
            return null;
        }

        return TestPackageResponse.builder()
                .id(testPackage.getId().toString())
                .name(testPackage.getName())
                .description(testPackage.getDescription())
                .imageUrl(testPackage.getImageUrl())
                .price(testPackage.getPrice().doubleValue())
                .discountPrice(testPackage.getDiscountPrice().doubleValue())
                .build();
    }

    private Date convertToDate(LocalDateTime localDateTime) {
        return localDateTime != null
                ? Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant())
                : null;
    }

    @Override
    public List<TestAttemptResponse> getTestAttemptByUserId(String userId) {
        UUID userUuid = UUID.fromString(userId);
        List<TestAttempt> attempts = testAttemptRepository.findByUserIdAndStatus(
                userUuid,
                TestAttemptStatus.COMPLETED
        );
        return attempts.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public TestAttempt getTestAttemptById(String id) {
        return testAttemptRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new NotFoundException("Test attempt with ID: " + id + " not found"));
    }

    @Override
    public Mono<String> getOrTriggerAIEvaluation(String testAttemptId) {
        TestAttempt attempt = getTestAttemptById(testAttemptId);

        if (attempt.getAiEvaluationResult() != null && !attempt.getAiEvaluationResult().isEmpty()) {
            return Mono.just(attempt.getAiEvaluationResult());
        }

        List<UserAnswerEvaluationRequest> userAnswerRequests = attempt.getUserAnswers().stream()
                .map(this::mapUserAnswerToEvaluationRequest)
                .collect(Collectors.toList());

        AIEvaluationRequest aiRequest = AIEvaluationRequest.builder()
                .testAttemptId(attempt.getId())
                .score(attempt.getScore())
                .userAnswers(userAnswerRequests)
                .build();

        return aiEvaluationService.getEvaluation(aiRequest)
                .doOnNext(evaluation -> {
                    attempt.setAiEvaluationResult(evaluation);
                    testAttemptRepository.save(attempt);
                });
    }
}