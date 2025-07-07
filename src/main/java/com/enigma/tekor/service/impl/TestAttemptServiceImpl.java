package com.enigma.tekor.service.impl;

import com.enigma.tekor.constant.TestAttemptStatus;
import com.enigma.tekor.constant.TransactionStatus;
import com.enigma.tekor.dto.request.AIEvaluationRequest;
import com.enigma.tekor.dto.request.SaveAnswerRequest;
import com.enigma.tekor.dto.request.UserAnswerEvaluationRequest;
import com.enigma.tekor.dto.response.InProgressAttempt;
import com.enigma.tekor.dto.response.ReadyTestPackage;
import com.enigma.tekor.dto.response.TestAttemptResponse;
import com.enigma.tekor.dto.response.TestPackageResponse;
import com.enigma.tekor.dto.response.UserTestAttemptResponse;
import com.enigma.tekor.entity.*;
import com.enigma.tekor.exception.AccessForbiddenException;
import com.enigma.tekor.exception.BadRequestException;
import com.enigma.tekor.exception.ConflictException;
import com.enigma.tekor.exception.NotFoundException;
import com.enigma.tekor.repository.TestAttemptRepository;
import com.enigma.tekor.repository.TestPackageRepository;
import com.enigma.tekor.repository.TransactionRepository;
import com.enigma.tekor.repository.UserRepository;
import com.enigma.tekor.security.CustomUserDetails;
import com.enigma.tekor.service.AIEvaluationService;
import com.enigma.tekor.service.TestAttemptService;
import com.enigma.tekor.service.UserAnswerService;
import com.enigma.tekor.service.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TestAttemptServiceImpl implements TestAttemptService {

    private static final Logger log = LoggerFactory.getLogger(TestAttemptServiceImpl.class);

    private final TestAttemptRepository testAttemptRepository;
    private final TestPackageRepository testPackageRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final UserAnswerService userAnswerService;
    private final UserService userService;
    private final AIEvaluationService aiEvaluationService;

    @Override
    @Transactional
    public TestAttemptResponse createTestAttempt(String packageId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User currentUser = userRepository.findById(userDetails.getUser().getId())
                .orElseThrow(() -> new NotFoundException("User not found"));

        TestPackage testPackageToAttempt = testPackageRepository.findById(UUID.fromString(packageId))
                .orElseThrow(() -> new NotFoundException("Test package with id " + packageId + " not found."));

        long allowedAttempts = countAllowedAttempts(currentUser, testPackageToAttempt);
        long existingAttempts = testAttemptRepository.countByUserAndTestPackage(currentUser, testPackageToAttempt);

        if (existingAttempts >= allowedAttempts) {
            if (allowedAttempts == 0) {
                throw new AccessForbiddenException("You have not purchased this test package.");
            } else {
                throw new ConflictException("You have already used all your available attempts for this test package. Please purchase again to retake.");
            }
        }

        TestAttempt newAttempt = TestAttempt.builder()
                .user(currentUser)
                .testPackage(testPackageToAttempt)
                .startTime(new Date())
                .status(TestAttemptStatus.IN_PROGRESS)
                .build();

        testAttemptRepository.save(newAttempt);

        return mapToResponse(newAttempt);
    }

    @Transactional
    @Override
    public void saveUserAnswer(String attemptId, SaveAnswerRequest request) {
        TestAttempt attempt = getTestAttemptEntityById(attemptId);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        if (!attempt.getUser().getId().equals(userDetails.getUser().getId())) {
            throw new AccessForbiddenException("You are not authorized to access this attempt");
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
        TestAttempt attempt = getTestAttemptEntityById(attemptId);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        if (!attempt.getUser().getId().equals(userDetails.getUser().getId())) {
            throw new AccessForbiddenException("You are not authorized to submit this attempt");
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
    
    @Override
    public TestAttempt getTestAttemptEntityById(String id) {
        return testAttemptRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new NotFoundException("Test attempt with ID: " + id + " not found"));
    }

    private long countAllowedAttempts(User user, TestPackage testPackage) {
        log.info("Checking allowed attempts for user ID: {} and test package ID: {}", user.getId(), testPackage.getId());
        List<Transaction> completedTransactions = transactionRepository.findByUserAndStatus(user, TransactionStatus.SUCCESS);
        log.info("Found {} completed transactions for user ID: {}", completedTransactions.size(), user.getId());
        
        long count = 0;
        for (Transaction trx : completedTransactions) {
            log.info("Processing transaction ID: {}, Status: {}", trx.getId(), trx.getStatus());
            if (trx.getTestPackage() != null) {
                log.info("Transaction has test package ID: {}", trx.getTestPackage().getId());
                if (trx.getTestPackage().getId().equals(testPackage.getId())) {
                    count++;
                    log.info("Match found for test package. Current count: {}", count);
                }
            }
            if (trx.getBundle() != null) {
                log.info("Transaction has bundle ID: {}", trx.getBundle().getId());
                boolean packageInBundle = trx.getBundle().getBundlePackages().stream()
                        .anyMatch(bundlePackage -> bundlePackage.getTestPackage().getId().equals(testPackage.getId()));
                if (packageInBundle) {
                    count++;
                    log.info("Match found for package in bundle. Current count: {}", count);
                }
            }
        }
        log.info("Final allowed attempts count for user ID: {} and test package ID: {}: {}", user.getId(), testPackage.getId(), count);
        return count;
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

    private TestPackageResponse mapToTestPackageResponse(TestPackage testPackage) {
        return TestPackageResponse.builder()
                .id(testPackage.getId().toString())
                .name(testPackage.getName())
                .description(testPackage.getDescription())
                .imageUrl(testPackage.getImageUrl())
                .price(testPackage.getPrice() != null ? testPackage.getPrice().doubleValue() : null)
                .discountPrice(testPackage.getDiscountPrice() != null ? testPackage.getDiscountPrice().doubleValue() : null)
                .build();
    }

    @Override
    public UserTestAttemptResponse getUserTestAttempts() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User currentUser = userDetails.getUser();

        List<TestAttempt> inProgressAttempts = testAttemptRepository.findByUserAndStatus(currentUser, TestAttemptStatus.IN_PROGRESS);
        List<InProgressAttempt> inProgress = inProgressAttempts.stream()
                .map(attempt -> InProgressAttempt.builder()
                        .attemptId(attempt.getId().toString())
                        .testPackage(mapToTestPackageResponse(attempt.getTestPackage()))
                        .startTime(attempt.getStartTime())
                        .remainingDuration(attempt.getRemainingDuration())
                        .build())
                .collect(Collectors.toList());

        List<Transaction> completedTransactions = transactionRepository.findByUserAndStatus(currentUser, TransactionStatus.SUCCESS);
        List<ReadyTestPackage> readyToStart = completedTransactions.stream()
                .filter(transaction -> {
                    TestPackage testPackage = transaction.getTestPackage();
                    if (testPackage == null && transaction.getBundle() != null) {
                        return transaction.getBundle().getBundlePackages().stream()
                                .anyMatch(bundlePackage ->
                                        testAttemptRepository.findByUserAndTestPackage(currentUser, bundlePackage.getTestPackage()).isEmpty()
                                );
                    } else if (testPackage != null) {
                        return testAttemptRepository.findByUserAndTestPackage(currentUser, testPackage).isEmpty();
                    }
                    return false;
                })
                .map(transaction -> {
                    TestPackage testPackage = transaction.getTestPackage();
                    if (testPackage == null && transaction.getBundle() != null) {
                        testPackage = transaction.getBundle().getBundlePackages().stream()
                                .filter(bundlePackage ->
                                        testAttemptRepository.findByUserAndTestPackage(currentUser, bundlePackage.getTestPackage()).isEmpty()
                                )
                                .map(BundlePackage::getTestPackage)
                                .findFirst()
                                .orElse(null);
                    }
                    return ReadyTestPackage.builder()
                            .transactionId(transaction.getId().toString())
                            .testPackage(mapToTestPackageResponse(testPackage))
                            .purchaseDate(Date.from(transaction.getCreatedAt().atZone(ZoneId.systemDefault()).toInstant()))
                            .build();
                })
                .collect(Collectors.toList());

        return UserTestAttemptResponse.builder()
                .inProgress(inProgress)
                .readyToStart(readyToStart)
                .build();
    }

    @Override
    public List<TestAttemptResponse> getCompletedTestAttempts() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User currentUser = userDetails.getUser();

        List<TestAttempt> completedAttempts = testAttemptRepository.findByUserIdAndStatus(currentUser.getId(), TestAttemptStatus.COMPLETED);

        return completedAttempts.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public Mono<String> getOrTriggerAIEvaluation(String testAttemptId) {
        TestAttempt attempt = getTestAttemptEntityById(testAttemptId);

        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userService.getByEmail(currentUsername);

        if (!attempt.getUser().getId().equals(currentUser.getId())) {
            throw new AccessForbiddenException("You are not authorized to access this evaluation");
        }

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