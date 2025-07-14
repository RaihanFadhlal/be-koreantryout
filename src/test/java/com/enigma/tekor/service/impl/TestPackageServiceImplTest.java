package com.enigma.tekor.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.enigma.tekor.dto.request.UpdateTestPackageRequest;
import com.enigma.tekor.dto.response.BundleResponse;
import com.enigma.tekor.dto.response.ProductResponse;
import com.enigma.tekor.dto.response.TestPackageResponse;
import com.enigma.tekor.entity.TestPackage;
import com.enigma.tekor.repository.QuestionRepository;
import com.enigma.tekor.repository.TestPackageRepository;
import com.enigma.tekor.service.BundleService;
import com.enigma.tekor.service.CloudinaryService;
import com.enigma.tekor.service.QuestionService;

@ExtendWith(MockitoExtension.class)
class TestPackageServiceImplTest {

    @Mock
    private TestPackageRepository testPackageRepository;

    @Mock
    private QuestionService questionService;

    @Mock
    private BundleService bundleService;

    @Mock
    private QuestionRepository questionRepository;

    @Mock
    private CloudinaryService cloudinaryService;

    @InjectMocks
    private TestPackageServiceImpl testPackageService;

    private TestPackage testPackage;

    @BeforeEach
    void setUp() {
        testPackage = new TestPackage();
        testPackage.setId(UUID.randomUUID());
        testPackage.setName("Test Package 1");
        testPackage.setPrice(BigDecimal.valueOf(100.0));
        testPackage.setDiscountPrice(BigDecimal.valueOf(80.0));
    }

    @Test
    void testGetById_Found() {
        // Given
        when(testPackageRepository.findById(testPackage.getId())).thenReturn(Optional.of(testPackage));

        // When
        var response = testPackageService.getById(testPackage.getId().toString());

        // Then
        assertNotNull(response);
        assertEquals(testPackage.getName(), response.getName());
        verify(testPackageRepository).findById(testPackage.getId());
    }

    @Test
    void testDelete_Success() {
        // Given
        when(testPackageRepository.findById(testPackage.getId())).thenReturn(Optional.of(testPackage));
        doNothing().when(testPackageRepository).delete(testPackage);

        // When
        testPackageService.delete(testPackage.getId().toString());

        // Then
        verify(testPackageRepository).delete(testPackage);
    }

    @Test
    void testUpdate_Success() {
        // Given
        UpdateTestPackageRequest request = new UpdateTestPackageRequest();
        request.setName("Updated Name");
        request.setDescription("Updated Desc");
        request.setPrice(120.0);
        request.setDiscountPrice(100.0);

        when(testPackageRepository.findById(testPackage.getId())).thenReturn(Optional.of(testPackage));
        when(testPackageRepository.save(any(TestPackage.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        TestPackageResponse response = testPackageService.update(testPackage.getId().toString(), request);

        // Then
        assertNotNull(response);
        assertEquals("Updated Name", response.getName());
        assertEquals(120.0, response.getPrice());
        verify(testPackageRepository).findById(testPackage.getId());
        verify(testPackageRepository).save(any(TestPackage.class));
    }

    @Test
    void testGetAllPackagesAndBundles() {
        // Given
        BundleResponse bundle = BundleResponse.builder()
            .id(UUID.randomUUID())
            .name("Test Bundle")
            .price(BigDecimal.valueOf(200.0))
            .build();

        when(testPackageRepository.findAll()).thenReturn(Collections.singletonList(testPackage));
        when(bundleService.getAll()).thenReturn(Collections.singletonList(bundle));

        // When
        List<ProductResponse> products = testPackageService.getAllPackagesAndBundles();

        // Then
        assertNotNull(products);
        assertEquals(2, products.size());
        assertTrue(products.stream().anyMatch(p -> p.getType().equals("package")));
        assertTrue(products.stream().anyMatch(p -> p.getType().equals("bundle")));
    }
}
