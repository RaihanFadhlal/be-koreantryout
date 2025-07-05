package com.enigma.tekor.controller;

import com.enigma.tekor.dto.request.SaveAnswerRequest;
import com.enigma.tekor.dto.request.TestAttemptRequest;
import com.enigma.tekor.dto.response.CommonResponse;
import com.enigma.tekor.dto.response.TestAttemptResponse;
import com.enigma.tekor.dto.response.UserTestAttemptResponse;
import com.enigma.tekor.entity.User;
import com.enigma.tekor.exception.BadRequestException;
import com.enigma.tekor.service.TestAttemptService;
import com.enigma.tekor.service.UserService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/test-attempts")
@RequiredArgsConstructor
public class TestAttemptController {

    private final TestAttemptService testAttemptService;
    private final UserService userService;

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
                .status(HttpStatus.OK.getReasonPhrase())
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
                .status(HttpStatus.OK.getReasonPhrase())
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


    @GetMapping("/{userId}/attempts")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<CommonResponse<UserTestAttemptResponse>> getUserTestAttempts(
            @PathVariable String userId) {

        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userService.getByEmail(currentUsername);

        if (!currentUser.getId().toString().equals(userId)) {
            throw new BadRequestException("You can only view your own test attempts");
        }

        UserTestAttemptResponse response = testAttemptService.getUserTestAttempt(userId);
        return ResponseEntity.ok(CommonResponse.<UserTestAttemptResponse>builder()
                .status(HttpStatus.OK.getReasonPhrase())
                .message("User test attempts retrieved successfully")
                .data(response)
                .build());
    }
    
    @GetMapping("/{userId}/completed-attempts")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<CommonResponse<List<TestAttemptResponse>>> getTestAttemptByUserId(
    @PathVariable String userId
    ) {

    String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
    User currentUser = userService.getByEmail(currentUsername);
    
    if (!currentUser.getId().toString().equals(userId) && 
        !SecurityContextHolder.getContext().getAuthentication()
            .getAuthorities().stream()
            .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
        throw new BadRequestException("Unauthorized access");
    }

    List<TestAttemptResponse> responses = testAttemptService.getTestAttemptByUserId(userId);
    return ResponseEntity.ok(CommonResponse.<List<TestAttemptResponse>>builder()
            .status(HttpStatus.OK.getReasonPhrase())
            .message("Completed test attempts retrieved successfully")
            .data(responses)
            .build());
}
}
