package com.enigma.tekor.service.impl;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.enigma.tekor.dto.response.*;
import com.enigma.tekor.entity.Question;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.enigma.tekor.constant.TestAttemptStatus;
import com.enigma.tekor.constant.TransactionStatus;
import com.enigma.tekor.dto.request.AIEvaluationRequest;
import com.enigma.tekor.dto.request.SaveAnswerRequest;
import com.enigma.tekor.dto.request.UserAnswerEvaluationRequest;
import com.enigma.tekor.entity.BundlePackage;
import com.enigma.tekor.entity.TestAttempt;
import com.enigma.tekor.entity.TestPackage;
import com.enigma.tekor.entity.Transaction;
import com.enigma.tekor.entity.User;
import com.enigma.tekor.entity.UserAnswer;
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
import reactor.core.publisher.Mono;

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
                throw new ConflictException(
                        "You have already used all your available attempts for this test package. Please purchase again to retake.");
            }
        }

        LocalDateTime now = LocalDateTime.now();
        TestAttempt newAttempt = TestAttempt.builder()
                .user(currentUser)
                .testPackage(testPackageToAttempt)
                .startTime(now)
                .finishTime(now.plusSeconds(3000))
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

        if (LocalDateTime.now().isAfter(attempt.getFinishTime())) {
            throw new BadRequestException("Waktu ujian telah berakhir. Jawaban tidak dapat disimpan.");
        }

        userAnswerService.saveAnswer(request, attempt);

        Integer score = userAnswerService.calculateScore(attempt);
        attempt.setScore(Float.valueOf(score));
        testAttemptRepository.save(attempt);
    }

    @Transactional
    @Override
    public SubmitAttemptResponse submitAttempt(String attemptId) {
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
        long totalCorrect = attempt.getUserAnswers().stream().filter(UserAnswer::getIsCorrect).count();
        int totalQuestions = attempt.getTestPackage().getQuestions().size();
        long totalIncorrect = totalQuestions - totalCorrect;

        attempt.setEndTime(LocalDateTime.now());
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

        return SubmitAttemptResponse.builder()
                .totalCorrect((int) totalCorrect)
                .totalIncorrect((int) totalIncorrect)
                .score(attempt.getScore())
                .completionTime(attempt.getEndTime())
                .build();
    }

    @Override
    public TestAttempt getTestAttemptEntityById(String id) {
        return testAttemptRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new NotFoundException("Test attempt with ID: " + id + " not found"));
    }

    private long countAllowedAttempts(User user, TestPackage testPackage) {
        List<Transaction> completedTransactions = transactionRepository.findByUserAndStatus(user, TransactionStatus.SUCCESS);
        long count = 0;
        for (Transaction trx : completedTransactions) {
            if (trx.getTestPackage() != null) {
                if (trx.getTestPackage().getId().equals(testPackage.getId())) {
                    count++;
                }
            }
            if (trx.getBundle() != null) {
                boolean packageInBundle = trx.getBundle().getBundlePackages().stream()
                        .anyMatch(bundlePackage -> bundlePackage.getTestPackage().getId().equals(testPackage.getId()));
                if (packageInBundle) {
                    count++;
                }
            }
        }
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
                .finishTime(testAttempt.getFinishTime())
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
                .discountPrice(
                        testPackage.getDiscountPrice() != null ? testPackage.getDiscountPrice().doubleValue() : null)
                .build();
    }

    @Override
    public UserTestAttemptResponse getUserTestAttempts() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User currentUser = userDetails.getUser();

        List<TestAttempt> inProgressAttempts = testAttemptRepository.findByUserAndStatus(currentUser,
                TestAttemptStatus.IN_PROGRESS);
        List<InProgressAttempt> inProgress = inProgressAttempts.stream()
                .map(attempt -> InProgressAttempt.builder()
                        .attemptId(attempt.getId().toString())
                        .testPackage(mapToTestPackageResponse(attempt.getTestPackage()))
                        .startTime(attempt.getStartTime())
                        .finishTime(attempt.getFinishTime())
                        .build())
                .collect(Collectors.toList());

        List<Transaction> completedTransactions = transactionRepository.findByUserAndStatus(currentUser,
                TransactionStatus.SUCCESS);
        List<TestAttempt> allAttempts = testAttemptRepository.findByUser(currentUser);

        Map<TestPackage, Long> purchasedCount = completedTransactions.stream()
                .flatMap(t -> {
                    if (t.getTestPackage() != null) {
                        return Stream.of(t.getTestPackage());
                    } else if (t.getBundle() != null) {
                        return t.getBundle().getBundlePackages().stream().map(BundlePackage::getTestPackage);
                    }
                    return Stream.empty();
                })
                .collect(Collectors.groupingBy(tp -> tp, Collectors.counting()));

        Map<TestPackage, Long> attemptedCount = allAttempts.stream()
                .collect(Collectors.groupingBy(TestAttempt::getTestPackage, Collectors.counting()));

        List<ReadyTestPackage> readyToStart = purchasedCount.entrySet().stream()
                .flatMap(entry -> {
                    TestPackage testPackage = entry.getKey();
                    long totalPurchased = entry.getValue();
                    long totalAttempted = attemptedCount.getOrDefault(testPackage, 0L);
                    long availableAttempts = totalPurchased - totalAttempted;

                    if (availableAttempts > 0) {
                        return completedTransactions.stream()
                                .filter(t -> (t.getTestPackage() != null && t.getTestPackage().equals(testPackage)) ||
                                        (t.getBundle() != null && t.getBundle().getBundlePackages().stream()
                                                .anyMatch(bp -> bp.getTestPackage().equals(testPackage))))
                                .limit(availableAttempts)
                                .map(transaction -> ReadyTestPackage.builder()
                                        .transactionId(transaction.getId().toString())
                                        .testPackage(mapToTestPackageResponse(testPackage))
                                        .purchaseDate(transaction.getCreatedAt())
                                        .build());
                    } else {
                        return Stream.empty();
                    }
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

        List<TestAttempt> completedAttempts = testAttemptRepository.findByUserIdAndStatus(currentUser.getId(),
                TestAttemptStatus.COMPLETED);

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

    @Override
    public TestAttemptDetailResponse getTestAttemptDetails(String attemptId) {
        TestAttempt attempt = getTestAttemptEntityById(attemptId);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        if (!attempt.getUser().getId().equals(userDetails.getUser().getId())) {
            throw new AccessForbiddenException("You are not authorized to access this attempt details");
        }

        if (!attempt.getStatus().equals(TestAttemptStatus.IN_PROGRESS)) {
            throw new BadRequestException(
                    "Test attempt is not in progress. Details can only be viewed for in-progress tests.");
        }

        List<QuestionResponse> questionResponses = attempt.getTestPackage().getQuestions().stream()
                .sorted(Comparator.comparing(Question::getNumber))
                .map(question -> QuestionResponse.builder()
                        .id(question.getId())
                        .questionText(question.getQuestionText())
                        .imageUrl(question.getImageUrl())
                        .audioUrl(question.getAudioUrl())
                        .questionType(question.getQuestionType())
                        .options(question.getOptions().stream()
                                .map(option -> OptionResponse.builder()
                                        .id(option.getId())
                                        .optionText(option.getOptionText())
                                        .build())
                                .collect(Collectors.toList()))
                        .build())
                .collect(Collectors.toList());

        List<UserAnswerResponse> userAnswerResponses = attempt.getUserAnswers().stream()
                .map(userAnswer -> UserAnswerResponse.builder()
                        .id(userAnswer.getId())
                        .questionId(userAnswer.getQuestion().getId())
                        .selectedOptionId(
                                userAnswer.getSelectedOption() != null ? userAnswer.getSelectedOption().getId() : null)
                        .build())
                .collect(Collectors.toList());

        return TestAttemptDetailResponse.builder()
                .id(attempt.getId())
                .testPackageName(attempt.getTestPackage().getName())
                .startTime(attempt.getStartTime())
                .finishTime(attempt.getFinishTime())
                .questions(questionResponses)
                .userAnswers(userAnswerResponses)
                .build();
    }
}