package com.enigma.tekor.service;

import java.util.List;

import com.enigma.tekor.dto.request.SaveAnswerRequest;
import com.enigma.tekor.dto.response.SubmitAttemptResponse;
import com.enigma.tekor.dto.response.TestAttemptDetailResponse;
import com.enigma.tekor.dto.response.TestAttemptResponse;
import com.enigma.tekor.dto.response.UserTestAttemptResponse;
import com.enigma.tekor.dto.response.TestAttemptReviewResponse;
import com.enigma.tekor.entity.TestAttempt;
import com.enigma.tekor.entity.Transaction;

import reactor.core.publisher.Mono;

public interface TestAttemptService {
    TestAttemptResponse createTestAttempt(String packageId);
    void saveUserAnswer(String attemptId, SaveAnswerRequest request);
    SubmitAttemptResponse submitAttempt(String attemptId);
    TestAttempt getTestAttemptEntityById(String id);
    UserTestAttemptResponse getUserTestAttempts();
    List<TestAttemptResponse> getCompletedTestAttempts();
    Mono<String> getOrTriggerAIEvaluation(String testAttemptId);
    TestAttemptDetailResponse getTestAttemptDetails(String attemptId);
    TestAttemptReviewResponse getTestAttemptReview(String testAttemptId);
}
