package com.enigma.tekor.service;

import com.enigma.tekor.dto.request.AIEvaluationRequest;

import reactor.core.publisher.Mono;

public interface AIEvaluationService {
    Mono<String> getEvaluation(AIEvaluationRequest request);
}
