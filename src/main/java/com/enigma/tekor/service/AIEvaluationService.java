package com.enigma.tekor.service;

import com.enigma.tekor.entity.TestAttempt;

import reactor.core.publisher.Mono;

public interface AIEvaluationService {
    Mono<String> getEvaluation(TestAttempt testAttempt);
}
