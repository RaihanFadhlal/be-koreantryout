package com.enigma.tekor.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.enigma.tekor.entity.Transaction;
import com.enigma.tekor.entity.User;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {
    Optional<Transaction> findByMidtransOrderId(String midtransOrderId);
    @Query("SELECT t FROM Transaction t LEFT JOIN FETCH t.testPackage LEFT JOIN FETCH t.bundle WHERE t.user = :user ORDER BY t.createdAt DESC")
    List<Transaction> findByUserWithDetails(@Param("user") User user);
}
