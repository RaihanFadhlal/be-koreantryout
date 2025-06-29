package com.enigma.tekor.service.impl;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.enigma.tekor.constant.QuestionType;
import com.enigma.tekor.entity.Question;
import com.enigma.tekor.repository.QuestionRepository;
import com.enigma.tekor.service.QuestionService;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class QuestionServiceImpl implements QuestionService {

    private final QuestionRepository questionRepository;
      

    @Override
    public Question create(Question question) {
        return questionRepository.save(question);
    }

    @Override
    public Question getById(UUID id) {
        return questionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Question not found with id: " + id));
    }

    @Override
    public List<Question> getAll() {
        return questionRepository.findAll();
    }

    @Override
    public List<Question> getByCategory(QuestionType category) {
        return questionRepository.findByQuestionType(category);
    }

    @Override
    public void delete(UUID id) {
        if (!questionRepository.existsById(id)) {
            throw new EntityNotFoundException("Question not found with id: " + id);
        }
        questionRepository.deleteById(id);
    }
    
}
