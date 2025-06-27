package com.enigma.tekor.repository;

import com.enigma.tekor.entity.TestPackage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TestPackageRepository extends JpaRepository<TestPackage, UUID> {
    TestPackage findByName(String name);
}
