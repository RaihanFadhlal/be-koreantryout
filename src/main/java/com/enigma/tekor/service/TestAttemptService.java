package com.enigma.tekor.service;

import com.enigma.tekor.dto.request.SaveAnswerRequest;
import com.enigma.tekor.dto.response.SubmitAttemptResponse;
import com.enigma.tekor.dto.response.TestAttemptDetailResponse;
import com.enigma.tekor.dto.response.TestAttemptResponse;
import com.enigma.tekor.dto.response.UserTestAttemptResponse;
import com.enigma.tekor.entity.TestAttempt;
import reactor.core.publisher.Mono;

import java.util.List;

public interface TestAttemptService {
    TestAttemptResponse createTestAttempt(String packageId);
    void saveUserAnswer(String attemptId, SaveAnswerRequest request);
    SubmitAttemptResponse submitAttempt(String attemptId);
    TestAttempt getTestAttemptEntityById(String id);
    UserTestAttemptResponse getUserTestAttempts();
    List<TestAttemptResponse> getCompletedTestAttempts();
    Mono<String> getOrTriggerAIEvaluation(String testAttemptId);
    TestAttemptDetailResponse getTestAttemptDetails(String attemptId);
}
