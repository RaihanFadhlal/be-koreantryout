package com.enigma.tekor.repository;

import com.enigma.tekor.constant.TestAttemptStatus;
import com.enigma.tekor.entity.TestAttempt;
import com.enigma.tekor.entity.Transaction;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TestAttemptRepository extends JpaRepository<TestAttempt, UUID> {

    boolean existsByTransaction(Transaction transaction);

    @Query("SELECT ta FROM TestAttempt ta " +
       "JOIN FETCH ta.testPackage " +
       "WHERE ta.user.id = :userId AND ta.status = :status")
    List<TestAttempt> findByUserIdAndStatus(
    @Param("userId") UUID userId,
    @Param("status") TestAttemptStatus status);

   
}
