package com.enigma.tekor.controller;

import com.enigma.tekor.dto.response.CommonResponse;
import com.enigma.tekor.service.TestAttemptService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/ai-evaluations")
@RequiredArgsConstructor
public class AIEvaluationController {

    private final TestAttemptService testAttemptService;

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/{testAttemptId}")
    public Mono<ResponseEntity<CommonResponse<String>>> getAIEvaluation(@PathVariable String testAttemptId) {
        return testAttemptService.getOrTriggerAIEvaluation(testAttemptId)
                .map(evaluation -> ResponseEntity.ok(
                        CommonResponse.<String>builder()
                                .status(HttpStatus.OK.getReasonPhrase())
                                .message("Successfully retrieved AI evaluation.")
                                .data(evaluation)
                                .build()
                ));
    }
}