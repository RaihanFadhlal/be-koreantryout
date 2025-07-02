package com.enigma.tekor.controller;

import com.enigma.tekor.dto.request.SaveAnswerRequest;
import com.enigma.tekor.dto.request.TestAttemptRequest;
import com.enigma.tekor.dto.response.CommonResponse;
import com.enigma.tekor.dto.response.TestAttemptResponse;
import com.enigma.tekor.service.TestAttemptService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/test-attempts")
@RequiredArgsConstructor
public class TestAttemptController {

    private final TestAttemptService testAttemptService;

    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<CommonResponse<TestAttemptResponse>> create(@RequestBody TestAttemptRequest request) {
        TestAttemptResponse response = testAttemptService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(CommonResponse.<TestAttemptResponse>builder()
                        .status(HttpStatus.CREATED.getReasonPhrase())
                        .message("Test attempt created successfully")
                        .data(response)
                        .build());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<CommonResponse<TestAttemptResponse>> getById(@PathVariable String id) {
        TestAttemptResponse response = testAttemptService.getById(id);
        return ResponseEntity.ok(CommonResponse.<TestAttemptResponse>builder()
                .status(HttpStatus.CREATED.getReasonPhrase())
                .message("Test attempt retrieved successfully")
                .data(response)
                .build());
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CommonResponse<List<TestAttemptResponse>>> getAll() {
        List<TestAttemptResponse> responses = testAttemptService.getAll();
        return ResponseEntity.ok(CommonResponse.<List<TestAttemptResponse>>builder()
                .status(HttpStatus.CREATED.getReasonPhrase())
                .message("Test attempts retrieved successfully")
                .data(responses)
                .build());
    }

    @PutMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<CommonResponse<TestAttemptResponse>> update(@RequestBody TestAttemptRequest request) {
        TestAttemptResponse response = testAttemptService.update(request);
        return ResponseEntity.ok(CommonResponse.<TestAttemptResponse>builder()
                .status(HttpStatus.CREATED.getReasonPhrase())
                .message("Test attempt updated successfully")
                .data(response)
                .build());
    }

    @PostMapping("/{attemptId}/answer")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<CommonResponse<String>> saveAnswer(
            @PathVariable String attemptId,
            @RequestBody SaveAnswerRequest request) {
        testAttemptService.saveUserAnswer(attemptId, request);
        return ResponseEntity.ok(CommonResponse.<String>builder()
                .status(HttpStatus.CREATED.getReasonPhrase())
                .message("Progress saved successfully")
                .data("OK")
                .build());
    }

    @PostMapping("/{attemptId}/submit")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<CommonResponse<String>> submitAttempt(@PathVariable String attemptId) {
        testAttemptService.submitAttempt(attemptId);
        return ResponseEntity.ok(CommonResponse.<String>builder()
                .status(HttpStatus.OK.getReasonPhrase())
                .message("Test attempt submitted successfully")
                .data("OK")
                .build());
    }
}
