package com.enigma.tekor.service;

import java.util.List;

import com.enigma.tekor.dto.request.SaveAnswerRequest;
import com.enigma.tekor.dto.request.TestAttemptRequest;
import com.enigma.tekor.dto.response.TestAttemptResponse;
import com.enigma.tekor.entity.TestAttempt;

public interface TestAttemptService {
    TestAttemptResponse create(TestAttemptRequest request);
    TestAttemptResponse getById(String id);
    List<TestAttemptResponse> getAll();
    TestAttemptResponse update(TestAttemptRequest request);
    void saveUserAnswer(String attemptId, SaveAnswerRequest request);
    void submitAttempt(String attemptId);
    TestAttempt getTestAttemptById(String id);
}
