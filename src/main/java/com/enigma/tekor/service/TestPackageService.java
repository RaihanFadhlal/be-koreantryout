package com.enigma.tekor.service;

import com.enigma.tekor.dto.request.CreateTestPackageRequest;
import com.enigma.tekor.dto.request.UpdateTestPackageRequest;
import com.enigma.tekor.dto.response.TestPackageResponse;

public interface TestPackageService {
    void createTestPackageFromExcel(CreateTestPackageRequest request);
    TestPackageResponse update(String id, UpdateTestPackageRequest request);
    TestPackageResponse getById(String id);
    void delete(String id);
}
