package com.enigma.tekor.service;

import com.enigma.tekor.dto.request.SaveAnswerRequest;
import com.enigma.tekor.entity.TestAttempt;
import com.enigma.tekor.entity.UserAnswer;

public interface UserAnswerService {
    UserAnswer saveAnswer(SaveAnswerRequest request, TestAttempt attempt);
    Integer calculateScore(TestAttempt attempt);

}
