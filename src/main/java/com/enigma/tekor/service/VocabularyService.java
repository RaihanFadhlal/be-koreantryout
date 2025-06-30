package com.enigma.tekor.service;

import java.util.List;
import java.util.UUID;

import com.enigma.tekor.constant.VocabularyCategories;
import com.enigma.tekor.dto.request.VocabularyRequest;
import com.enigma.tekor.dto.response.VocabularyResponse;
import com.enigma.tekor.entity.Vocabulary;

public interface VocabularyService {
     List<String> getAllCategoryNames();

     List<Vocabulary> getVocabulariesByCategory(VocabularyCategories category);
     
     VocabularyResponse createVocabularyFromExcel(VocabularyRequest request);
    
     void deleteVocabularyById(UUID id);
    
    
}
