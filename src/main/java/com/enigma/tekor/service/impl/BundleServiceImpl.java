package com.enigma.tekor.service.impl;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.enigma.tekor.dto.request.BundleRequest;
import com.enigma.tekor.dto.request.BundleUpdateRequest;
import com.enigma.tekor.dto.response.BundleResponse;
import com.enigma.tekor.dto.response.PackageInBundleResponse;
import com.enigma.tekor.entity.Bundle;
import com.enigma.tekor.entity.BundlePackage;
import com.enigma.tekor.entity.TestPackage;
import com.enigma.tekor.exception.BadRequestException;
import com.enigma.tekor.exception.ConflictException;
import com.enigma.tekor.exception.NotFoundException;
import com.enigma.tekor.repository.BundleRepository;
import com.enigma.tekor.repository.TransactionRepository;
import com.enigma.tekor.service.BundlePackageService;
import com.enigma.tekor.service.BundleService;
import com.enigma.tekor.service.TestPackageService;

@Service
public class BundleServiceImpl implements BundleService {

    private final BundleRepository bundleRepository;
    private final TestPackageService testPackageService;
    private final BundlePackageService bundlePackageService;
    private final TransactionRepository transactionRepository;

    public BundleServiceImpl(
            BundleRepository bundleRepository,
            @Lazy TestPackageService testPackageService,
            BundlePackageService bundlePackageService,
            TransactionRepository transactionRepository
    ) {
        this.bundleRepository = bundleRepository;
        this.testPackageService = testPackageService;
        this.bundlePackageService = bundlePackageService;
        this.transactionRepository = transactionRepository;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public BundleResponse create(BundleRequest request) {
        if (request.getPackageIds() == null || request.getPackageIds().size() < 2) {
            throw new BadRequestException("Bundle must have at least two packages");
        }

        Bundle bundle = new Bundle();
        bundle.setName(request.getName());
        bundle.setDescription(request.getDescription());
        bundle.setPrice(request.getPrice());
        bundle.setDiscountPrice(request.getDiscountPrice());
        bundle.setImageUrl(request.getImageUrl());
        bundleRepository.saveAndFlush(bundle);

        List<BundlePackage> bundlePackages = request.getPackageIds().stream().map(packageId -> {
            TestPackage testPackage = testPackageService.getOneById(packageId);
            BundlePackage bundlePackage = new BundlePackage();
            bundlePackage.setBundle(bundle);
            bundlePackage.setTestPackage(testPackage);
            return bundlePackageService.save(bundlePackage);
        }).collect(Collectors.toList());
        bundle.setBundlePackages(bundlePackages);

        return toBundleResponse(bundle);
    }

    @Override
    public List<BundleResponse> getAll() {
        return bundleRepository.findAll().stream()
                .map(this::toBundleResponse)
                .collect(Collectors.toList());
    }

    @Override
    public BundleResponse getById(UUID id) {
        Bundle bundle = bundleRepository.findById(id).orElseThrow(() -> new NotFoundException("Bundle not found"));
        return toBundleResponse(bundle);
    }

    @Override
    public Bundle getBundleById(UUID id) {
        return bundleRepository.findById(id).orElseThrow(() -> new NotFoundException("Bundle not found"));
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public BundleResponse update(UUID id, BundleUpdateRequest request) {
        Bundle bundle = bundleRepository.findById(id).orElseThrow(() -> new NotFoundException("Bundle not found"));

        if (request.getName() != null) {
            bundle.setName(request.getName());
        }
        if (request.getDescription() != null) {
            bundle.setDescription(request.getDescription());
        }
        if (request.getPrice() != null) {
            bundle.setPrice(request.getPrice());
        }
        if (request.getDiscountPrice() != null) {
            bundle.setDiscountPrice(request.getDiscountPrice());
        }

        bundleRepository.save(bundle);
        return toBundleResponse(bundle);
    }

    private BundleResponse toBundleResponse(Bundle bundle) {
        List<PackageInBundleResponse> packageResponses = bundle.getBundlePackages().stream()
                .map(bundlePackage -> PackageInBundleResponse.builder()
                        .id(bundlePackage.getTestPackage().getId())
                        .name(bundlePackage.getTestPackage().getName())
                        .build())
                .collect(Collectors.toList());

        return BundleResponse.builder()
                .id(bundle.getId())
                .name(bundle.getName())
                .description(bundle.getDescription())
                .price(bundle.getPrice())
                .discountPrice(bundle.getDiscountPrice())
                .imageUrl(bundle.getImageUrl())
                .packages(packageResponses)
                .build();
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void delete(UUID id) {
        Bundle bundle = bundleRepository.findById(id).orElseThrow(() -> new NotFoundException("Bundle not found"));
        bundleRepository.delete(bundle);
    }
}
