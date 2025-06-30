package com.enigma.tekor.controller;

import com.enigma.tekor.dto.request.CreateTestPackageRequest;
import com.enigma.tekor.dto.response.CommonResponse;
import com.enigma.tekor.service.TestPackageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
}
