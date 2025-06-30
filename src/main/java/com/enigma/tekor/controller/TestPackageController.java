package com.enigma.tekor.controller;

import com.enigma.tekor.dto.request.CreateTestPackageRequest;
import com.enigma.tekor.dto.response.CommonResponse;
import com.enigma.tekor.service.TestPackageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.enigma.tekor.dto.request.UpdateTestPackageRequest;
import com.enigma.tekor.dto.response.TestPackageResponse;

@RestController
@RequestMapping("/api/v1/test-packages")
@RequiredArgsConstructor
public class TestPackageController {

    private final TestPackageService testPackageService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CommonResponse<Void>> createTestPackage(
            @ModelAttribute CreateTestPackageRequest request
    ) {
        testPackageService.createTestPackageFromExcel(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(CommonResponse.<Void>builder()
                .status(HttpStatus.CREATED.name())
                .message("Test package created successfully")
                .build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<CommonResponse<TestPackageResponse>> updateTestPackage(@PathVariable String id, @RequestBody UpdateTestPackageRequest request) {
        TestPackageResponse updatedTestPackage = testPackageService.update(id, request);
        return ResponseEntity.ok(CommonResponse.<TestPackageResponse>builder()
                .status(HttpStatus.OK.name())
                .message("Test package updated successfully")
                .data(updatedTestPackage)
                .build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CommonResponse<TestPackageResponse>> getTestPackageById(@PathVariable String id) {
        TestPackageResponse testPackage = testPackageService.getById(id);
        return ResponseEntity.ok(CommonResponse.<TestPackageResponse>builder()
                .status(HttpStatus.OK.name())
                .message("Test package retrieved successfully")
                .data(testPackage)
                .build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<CommonResponse<Void>> deleteTestPackage(@PathVariable String id) {
        testPackageService.delete(id);
        return ResponseEntity.ok(CommonResponse.<Void>builder()
                .status(HttpStatus.OK.name())
                .message("Test package deleted successfully")
                .build());
    }
}
