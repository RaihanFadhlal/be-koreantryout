package com.enigma.tekor.service;

import com.enigma.tekor.dto.request.SaveAnswerRequest;
import com.enigma.tekor.dto.request.TestAttemptRequest;
import com.enigma.tekor.dto.response.TestAttemptResponse;

import java.util.List;

public interface TestAttemptService {

    TestAttemptResponse create(TestAttemptRequest request);
    TestAttemptResponse getById(String id);
    List<TestAttemptResponse> getAll();
    TestAttemptResponse update(TestAttemptRequest request);
    void saveUserAnswer(String attemptId, SaveAnswerRequest request);
    void submitAttempt(String attemptId);

}
