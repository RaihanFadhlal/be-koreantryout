package com.enigma.tekor.service.impl;

import com.enigma.tekor.entity.BundlePackage;
import com.enigma.tekor.repository.BundlePackageRepository;
import com.enigma.tekor.service.BundlePackageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BundlePackageServiceImpl implements BundlePackageService {

    private final BundlePackageRepository bundlePackageRepository;

    @Override
    @Transactional
    public BundlePackage save(BundlePackage bundlePackage) {
        return bundlePackageRepository.save(bundlePackage);
    }
}
