package com.enigma.tekor.service;


import java.util.List;
import java.util.UUID;

import com.enigma.tekor.entity.Option;

public interface OptionService {

    Option create(Option option);
    Option getById(UUID id);
    List<Option> getByQuestionId(UUID questionId);
    void delete(UUID id);
    
}
