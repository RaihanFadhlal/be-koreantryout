package com.enigma.tekor.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.enigma.tekor.constant.TestAttemptStatus;
import com.enigma.tekor.entity.TestAttempt;
import com.enigma.tekor.entity.TestPackage;
import com.enigma.tekor.entity.User;

@Repository
public interface TestAttemptRepository extends JpaRepository<TestAttempt, UUID> {

    long countByUserAndTestPackage(User user, TestPackage testPackage);

    @Query("SELECT ta FROM TestAttempt ta " +
       "JOIN FETCH ta.testPackage " +
       "WHERE ta.user.id = :userId AND ta.status = :status")
    List<TestAttempt> findByUserIdAndStatus(
    @Param("userId") UUID userId,
    @Param("status") TestAttemptStatus status);

    List<TestAttempt> findByUserAndStatus(User user, TestAttemptStatus status);
    List<TestAttempt> findByUserAndTestPackage(User user, TestPackage testPackage);
    List<TestAttempt> findByUser(User user);
}
