package com.enigma.tekor.repository;

import com.enigma.tekor.entity.Question;
import com.enigma.tekor.entity.TestAttempt;
import com.enigma.tekor.entity.UserAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserAnswerRepository extends JpaRepository<UserAnswer, UUID> {
    Optional<UserAnswer> findByTestAttemptAndQuestion(TestAttempt testAttempt, Question question);
    List<UserAnswer> findByTestAttemptId(UUID fromString);
}
