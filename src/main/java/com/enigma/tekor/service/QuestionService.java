package com.enigma.tekor.service;

import java.util.List;
import java.util.UUID;

import com.enigma.tekor.constant.QuestionType;
import com.enigma.tekor.dto.request.CreateQuestionRequest;
import com.enigma.tekor.entity.Question;

public interface QuestionService {
    Question create(Question question);
    Question getById(UUID id);
    List<Question> getAll();
    List<Question> getByCategory(QuestionType category);
    void delete(UUID id);
    Question createQuestionWithOptions(CreateQuestionRequest request);
}
