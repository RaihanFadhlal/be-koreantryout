package com.enigma.tekor.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.enigma.tekor.dto.request.BundleRequest;
import com.enigma.tekor.dto.request.BundleUpdateRequest;
import com.enigma.tekor.entity.Bundle;
import com.enigma.tekor.entity.BundlePackage;
import com.enigma.tekor.entity.TestPackage;
import com.enigma.tekor.exception.BadRequestException;
import com.enigma.tekor.exception.ConflictException;
import com.enigma.tekor.repository.BundleRepository;
import com.enigma.tekor.repository.TransactionRepository;
import com.enigma.tekor.service.BundlePackageService;
import com.enigma.tekor.service.TestPackageService;

@ExtendWith(MockitoExtension.class)
class BundleServiceImplTest {

    @Mock
    private BundleRepository bundleRepository;

    @Mock
    private TestPackageService testPackageService;

    @Mock
    private BundlePackageService bundlePackageService;

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private BundleServiceImpl bundleService;

    private Bundle bundle;

    @BeforeEach
    void setUp() {
        bundle = new Bundle();
        bundle.setId(UUID.randomUUID());
        bundle.setName("Test Bundle");
        bundle.setBundlePackages(Collections.emptyList());
    }

    @Test
    void testGetById_Found() {
        // Given
        when(bundleRepository.findById(bundle.getId())).thenReturn(Optional.of(bundle));

        // When
        var response = bundleService.getById(bundle.getId());

        // Then
        assertNotNull(response);
        assertEquals(bundle.getName(), response.getName());
        verify(bundleRepository).findById(bundle.getId());
    }

    @Test
    void testGetAll() {
        // Given
        when(bundleRepository.findAll()).thenReturn(Collections.singletonList(bundle));

        // When
        var responses = bundleService.getAll();

        // Then
        assertNotNull(responses);
        assertEquals(1, responses.size());
        verify(bundleRepository).findAll();
    }

    @Test
    void testCreate_Success() {
        // Given
        BundleRequest request = new BundleRequest();
        request.setName("New Bundle");
        request.setPackageIds(Arrays.asList(UUID.randomUUID(), UUID.randomUUID()));
        request.setPrice(BigDecimal.TEN);

        when(testPackageService.getOneById(any(UUID.class))).thenReturn(new TestPackage());
        when(bundleRepository.saveAndFlush(any(Bundle.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        var response = bundleService.create(request);

        // Then
        assertNotNull(response);
        assertEquals("New Bundle", response.getName());
        verify(bundleRepository).saveAndFlush(any(Bundle.class));
        verify(bundlePackageService, times(2)).save(any(BundlePackage.class));
    }

    @Test
    void testCreate_ThrowsBadRequest_WhenLessThanTwoPackages() {
        // Given
        BundleRequest request = new BundleRequest();
        request.setPackageIds(Collections.singletonList(UUID.randomUUID()));

        // When & Then
        assertThrows(BadRequestException.class, () -> bundleService.create(request));
    }

    @Test
    void testUpdate_Success() {
        // Given
        BundleUpdateRequest request = new BundleUpdateRequest();
        request.setName("Updated Name");

        when(bundleRepository.findById(bundle.getId())).thenReturn(Optional.of(bundle));
        when(bundleRepository.save(any(Bundle.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        var response = bundleService.update(bundle.getId(), request);

        // Then
        assertNotNull(response);
        assertEquals("Updated Name", response.getName());
        verify(bundleRepository).save(any(Bundle.class));
    }

    @Test
    void testDelete_Success() {
        // Given
        when(bundleRepository.findById(bundle.getId())).thenReturn(Optional.of(bundle));
        when(transactionRepository.existsByBundleId(bundle.getId())).thenReturn(false);
        doNothing().when(bundleRepository).delete(bundle);

        // When
        bundleService.delete(bundle.getId());

        // Then
        verify(bundleRepository).delete(bundle);
    }

    @Test
    void testDelete_ThrowsConflictException_WhenBundleHasTransactions() {
        // Given
        when(bundleRepository.findById(bundle.getId())).thenReturn(Optional.of(bundle));
        when(transactionRepository.existsByBundleId(bundle.getId())).thenReturn(true);

        // When & Then
        assertThrows(ConflictException.class, () -> bundleService.delete(bundle.getId()));
        verify(bundleRepository, never()).delete(any(Bundle.class));
    }
}
