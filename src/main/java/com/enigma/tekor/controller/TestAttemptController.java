package com.enigma.tekor.controller;

import com.enigma.tekor.dto.request.SaveAnswerRequest;
import java.util.List;
import com.enigma.tekor.dto.response.CommonResponse;
import com.enigma.tekor.dto.response.SubmitAttemptResponse;
import com.enigma.tekor.dto.response.TestAttemptDetailResponse;
import com.enigma.tekor.dto.response.TestAttemptResponse;
import com.enigma.tekor.dto.response.TestAttemptReviewResponse;
import com.enigma.tekor.dto.response.UserTestAttemptResponse;
import com.enigma.tekor.service.TestAttemptService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/test-attempts")
@RequiredArgsConstructor
public class TestAttemptController {

    private final TestAttemptService testAttemptService;

    @PostMapping("/start/{packageId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<CommonResponse<TestAttemptResponse>> createTestAttempt(@PathVariable String packageId) {
        TestAttemptResponse response = testAttemptService.createTestAttempt(packageId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(CommonResponse.<TestAttemptResponse>builder()
                        .status(HttpStatus.CREATED.getReasonPhrase())
                        .message("Test attempt created successfully")
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
                .status(HttpStatus.OK.getReasonPhrase())
                .message("Progress saved successfully")
                .data("OK")
                .build());
    }

    @PostMapping("/{attemptId}/submit")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<CommonResponse<SubmitAttemptResponse>> submitAttempt(@PathVariable String attemptId) {
        SubmitAttemptResponse response = testAttemptService.submitAttempt(attemptId);
        return ResponseEntity.ok(CommonResponse.<SubmitAttemptResponse>builder()
                .status(HttpStatus.OK.getReasonPhrase())
                .message("Test attempt submitted successfully")
                .data(response)
                .build());
    }

    @GetMapping("/my-tests")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<CommonResponse<UserTestAttemptResponse>> getMyTests() {
        UserTestAttemptResponse response = testAttemptService.getUserTestAttempts();
        return ResponseEntity.ok(CommonResponse.<UserTestAttemptResponse>builder()
                .status(HttpStatus.OK.getReasonPhrase())
                .message("Successfully retrieved user tests")
                .data(response)
                .build());
    }

    @GetMapping("/my-tests/completed")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<CommonResponse<List<TestAttemptResponse>>> getCompletedTests() {
        List<TestAttemptResponse> response = testAttemptService.getCompletedTestAttempts();
        return ResponseEntity.ok(CommonResponse.<List<TestAttemptResponse>>builder()
                .status(HttpStatus.OK.getReasonPhrase())
                .message("Successfully retrieved completed user tests")
                .data(response)
                .build());
    }

    @GetMapping("/{attemptId}/details")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<CommonResponse<TestAttemptDetailResponse>> getTestAttemptDetails(@PathVariable String attemptId) {
        TestAttemptDetailResponse response = testAttemptService.getTestAttemptDetails(attemptId);
        return ResponseEntity.ok(CommonResponse.<TestAttemptDetailResponse>builder()
                .status(HttpStatus.OK.getReasonPhrase())
                .message("Successfully retrieved test attempt details")
                .data(response)
                .build());
    }

    @GetMapping("/{attemptId}/review")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<CommonResponse<TestAttemptReviewResponse>> getTestAttemptReview(@PathVariable String attemptId) {
        TestAttemptReviewResponse response = testAttemptService.getTestAttemptReview(attemptId);
        return ResponseEntity.ok(CommonResponse.<TestAttemptReviewResponse>builder()
                .status(HttpStatus.OK.getReasonPhrase())
                .message("Successfully retrieved test attempt review")
                .data(response)
                .build());
    }
}
