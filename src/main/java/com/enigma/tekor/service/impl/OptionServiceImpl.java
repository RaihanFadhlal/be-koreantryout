package com.enigma.tekor.service.impl;



import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.enigma.tekor.entity.Option;
import com.enigma.tekor.repository.OptionRepository;
import com.enigma.tekor.repository.QuestionRepository;
import com.enigma.tekor.service.OptionService;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OptionServiceImpl implements OptionService {

     private final OptionRepository optionRepository;
     private final QuestionRepository questionRepository;
    

      @Override
    public Option create(Option option) {
        if (!questionRepository.existsById(option.getQuestion().getId())) {
            throw new EntityNotFoundException("Question not found with id: " + option.getQuestion().getId());
        }
        return optionRepository.save(option);
    }

    @Override
    public Option getById(UUID id) {
        return optionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Option not found with id: " + id));
    }

    @Override
    public List<Option> getByQuestionId(UUID questionId) {
        return optionRepository.findByQuestionId(questionId);
    }

    @Override
    public void delete(UUID id) {
        if (!optionRepository.existsById(id)) {
            throw new EntityNotFoundException("Option not found with id: " + id);
        }
        optionRepository.deleteById(id);
    }
    
}
