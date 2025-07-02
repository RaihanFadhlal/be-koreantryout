package com.enigma.tekor.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.enigma.tekor.dto.request.TestAttemptRequest;
import com.enigma.tekor.dto.response.CommonResponse;
import com.enigma.tekor.dto.response.TestAttemptResponse;
import com.enigma.tekor.service.TestAttemptService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/test-attempts")
@RequiredArgsConstructor
public class TestAttemptController {

    private final TestAttemptService testAttemptService;
     
    @PostMapping
    public ResponseEntity<CommonResponse<TestAttemptResponse>> create(@RequestBody TestAttemptRequest request) {
        TestAttemptResponse response = testAttemptService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(CommonResponse.<TestAttemptResponse>builder()
                        .status(String.valueOf(HttpStatus.CREATED.value()))
                        .message("Test attempt created successfully")
                        .data(response)
                        .build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CommonResponse<TestAttemptResponse>> getById(@PathVariable String id) {
        TestAttemptResponse response = testAttemptService.getById(id);
        return ResponseEntity.ok(CommonResponse.<TestAttemptResponse>builder()
                .status(String.valueOf(HttpStatus.OK.value()))
                .message("Test attempt retrieved successfully")
                .data(response)
                .build());
    }

    @GetMapping
    public ResponseEntity<CommonResponse<List<TestAttemptResponse>>> getAll() {
        List<TestAttemptResponse> responses = testAttemptService.getAll();
        return ResponseEntity.ok(CommonResponse.<List<TestAttemptResponse>>builder()
                .status(String.valueOf(HttpStatus.OK.value()))
                .message("Test attempts retrieved successfully")
                .data(responses)
                .build());
    }

    @PutMapping
    public ResponseEntity<CommonResponse<TestAttemptResponse>> update(@RequestBody TestAttemptRequest request) {
        TestAttemptResponse response = testAttemptService.update(request);
        return ResponseEntity.ok(CommonResponse.<TestAttemptResponse>builder()
                .status(String.valueOf(HttpStatus.OK.value()))
                .message("Test attempt updated successfully")
                .data(response)
                .build());
    }
}
