package com.enigma.tekor.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.enigma.tekor.dto.request.BundleRequest;
import com.enigma.tekor.dto.request.BundleUpdateRequest;
import com.enigma.tekor.dto.response.BundleResponse;
import com.enigma.tekor.dto.response.CommonResponse;
import com.enigma.tekor.service.BundleService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/bundles")
@RequiredArgsConstructor
public class BundleController {

    private final BundleService bundleService;

    @PreAuthorize("hasAnyRole('ADMIN')")
    @PostMapping
    public ResponseEntity<CommonResponse<BundleResponse>> createBundle(@RequestBody BundleRequest request) {
        BundleResponse bundleResponse = bundleService.create(request);
        CommonResponse<BundleResponse> response = CommonResponse.<BundleResponse>builder()
                .status(HttpStatus.CREATED.getReasonPhrase())
                .message("Successfully created bundle")
                .data(bundleResponse)
                .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<CommonResponse<List<BundleResponse>>> getAllBundles() {
        List<BundleResponse> bundleResponses = bundleService.getAll();
        CommonResponse<List<BundleResponse>> response = CommonResponse.<List<BundleResponse>>builder()
                .status(HttpStatus.OK.getReasonPhrase())
                .message("Successfully get all bundles")
                .data(bundleResponses)
                .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CommonResponse<BundleResponse>> getBundleById(@PathVariable UUID id) {
        BundleResponse bundleResponse = bundleService.getById(id);
        CommonResponse<BundleResponse> response = CommonResponse.<BundleResponse>builder()
                .status(HttpStatus.OK.getReasonPhrase())
                .message("Successfully get bundle by id")
                .data(bundleResponse)
                .build();
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyRole('ADMIN')")
    @PatchMapping("/{id}")
    public ResponseEntity<CommonResponse<BundleResponse>> updateBundle(@PathVariable UUID id, @RequestBody BundleUpdateRequest request) {
        BundleResponse bundleResponse = bundleService.update(id, request);
        CommonResponse<BundleResponse> response = CommonResponse.<BundleResponse>builder()
                .status(HttpStatus.OK.getReasonPhrase())
                .message("Successfully updated bundle")
                .data(bundleResponse)
                .build();
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<CommonResponse<String>> deleteBundle(@PathVariable UUID id) {
        bundleService.delete(id);
        CommonResponse<String> response = CommonResponse.<String>builder()
                .status(HttpStatus.OK.getReasonPhrase())
                .message("Successfully deleted bundle")
                .build();
        return ResponseEntity.ok(response);
    }
}
