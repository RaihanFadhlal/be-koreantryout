package com.enigma.tekor.service;

import java.util.List;

import com.enigma.tekor.dto.request.TestAttemptRequest;
import com.enigma.tekor.dto.response.TestAttemptResponse;

public interface TestAttemptService {

    TestAttemptResponse create(TestAttemptRequest request);
    TestAttemptResponse getById(String id);
    List<TestAttemptResponse> getAll();
    TestAttemptResponse update(TestAttemptRequest request);
    
}
