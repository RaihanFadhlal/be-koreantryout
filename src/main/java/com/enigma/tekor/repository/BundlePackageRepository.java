package com.enigma.tekor.repository;

import com.enigma.tekor.entity.BundlePackage;
import com.enigma.tekor.entity.BundlePackageId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BundlePackageRepository extends JpaRepository<BundlePackage, BundlePackageId> {
}
