package com.enigma.tekor.service;

import com.enigma.tekor.dto.response.ProductResponse;
import com.enigma.tekor.dto.request.CreateTestPackageRequest;
import com.enigma.tekor.dto.request.UpdateTestPackageRequest;
import com.enigma.tekor.dto.response.TestPackageResponse;
import com.enigma.tekor.entity.TestPackage;

import java.util.List;
import java.util.UUID;

public interface TestPackageService {
    void createTestPackageFromExcel(CreateTestPackageRequest request);
    TestPackageResponse update(String id, UpdateTestPackageRequest request);
    TestPackageResponse getById(String id);
    TestPackage getOneById(UUID id);
    void delete(String id);
    List<ProductResponse> getAllPackagesAndBundles();
    Integer getTotalQuestionsByPackageId(String packageId);
}
