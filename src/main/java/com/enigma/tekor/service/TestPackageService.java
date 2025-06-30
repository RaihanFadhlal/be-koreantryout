package com.enigma.tekor.service;

import com.enigma.tekor.dto.request.CreateTestPackageRequest;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;

public interface TestPackageService {
    void createTestPackageFromExcel(CreateTestPackageRequest request);
}
