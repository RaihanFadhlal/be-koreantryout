package com.enigma.tekor.repository;


import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.enigma.tekor.constant.QuestionType;
import com.enigma.tekor.entity.Question;

@Repository
public interface QuestionRepository extends JpaRepository<Question, UUID> {
    List<Question> findByQuestionType(QuestionType questionType);
}
