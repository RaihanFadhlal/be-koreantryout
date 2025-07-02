package com.enigma.tekor.repository;

import com.enigma.tekor.entity.TestAttempt;
import com.enigma.tekor.entity.Transaction;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TestAttemptRepository extends JpaRepository<TestAttempt, UUID> {

    boolean existsByTransaction(Transaction transaction);
}
