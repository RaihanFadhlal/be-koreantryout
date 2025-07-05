package com.enigma.tekor.repository;

import com.enigma.tekor.entity.TestEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TestEventRepository extends JpaRepository<TestEvent, UUID> {
}
