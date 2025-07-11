package com.enigma.tekor.controller;

import com.enigma.tekor.dto.response.CommonResponse;
import com.enigma.tekor.dto.response.ProductResponse;
import com.enigma.tekor.dto.request.CreateTestPackageRequest;
import com.enigma.tekor.dto.request.UpdateTestPackageRequest;
import com.enigma.tekor.dto.response.TestPackageResponse;
import com.enigma.tekor.service.TestPackageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/test-packages")
@RequiredArgsConstructor
public class TestPackageController {
    private final TestPackageService testPackageService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CommonResponse<String>> createTestPackageFromExcel(@ModelAttribute CreateTestPackageRequest request) {
        testPackageService.createTestPackageFromExcel(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(CommonResponse.<String>builder()
                        .status(HttpStatus.CREATED.getReasonPhrase())
                        .message("Successfully created test package")
                        .build());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping(path = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CommonResponse<TestPackageResponse>> updateTestPackage(@PathVariable String id, @ModelAttribute UpdateTestPackageRequest request) {
        TestPackageResponse updated = testPackageService.update(id, request);
        return ResponseEntity.ok(CommonResponse.<TestPackageResponse>builder()
                .status(HttpStatus.OK.getReasonPhrase())
                .message("Successfully updated test package")
                .data(updated)
                .build());
    }

    @GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CommonResponse<TestPackageResponse>> getTestPackageById(@PathVariable String id) {
        TestPackageResponse testPackageResponse = testPackageService.getById(id);
        return ResponseEntity.ok(CommonResponse.<TestPackageResponse>builder()
                .status(HttpStatus.OK.getReasonPhrase())
                .message("Successfully get test package by id")
                .data(testPackageResponse)
                .build());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CommonResponse<String>> deleteTestPackage(@PathVariable String id) {
        testPackageService.delete(id);
        return ResponseEntity.ok(CommonResponse.<String>builder()
                .status(HttpStatus.OK.getReasonPhrase())
                .message("Successfully deleted test package")
                .build());
    }

    @GetMapping
    public ResponseEntity<CommonResponse<List<ProductResponse>>> getAllPackagesAndBundles() {
        List<ProductResponse> productResponses = testPackageService.getAllPackagesAndBundles();
        return ResponseEntity.ok(CommonResponse.<List<ProductResponse>>builder()
                .status(HttpStatus.OK.getReasonPhrase())
                .message("Successfully get all packages and bundles")
                .data(productResponses)
                .build());
    }

    @GetMapping("/all")
    public ResponseEntity<CommonResponse<List<TestPackageResponse>>> getAllTestPackages() {
        List<TestPackageResponse> testPackageResponses = testPackageService.getAllTestPackages();
        return ResponseEntity.ok(CommonResponse.<List<TestPackageResponse>>builder()
                .status(HttpStatus.OK.getReasonPhrase())
                .message("Successfully get all test packages")
                .data(testPackageResponses)
                .build());
    }
}
