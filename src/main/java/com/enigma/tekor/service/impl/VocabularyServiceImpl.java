package com.enigma.tekor.service.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;

import com.enigma.tekor.constant.VocabularyCategories;
import com.enigma.tekor.dto.request.VocabularyRequest;
import com.enigma.tekor.dto.response.VocabularyResponse;
import com.enigma.tekor.entity.Vocabulary;
import com.enigma.tekor.repository.VocabularyRepository;
import com.enigma.tekor.service.VocabularyService;

import lombok.RequiredArgsConstructor;



@Service
@RequiredArgsConstructor
public class VocabularyServiceImpl implements VocabularyService {
    private final VocabularyRepository vocabularyRepository;

    @Override
    public List<String> getAllCategoryNames() {
        return Arrays.stream(VocabularyCategories.values())
                .map(Enum::name)
                .collect(Collectors.toList());
    }

    @Override
    public List<Vocabulary> getVocabulariesByCategory(VocabularyCategories category) {
        return vocabularyRepository.findByVocabularyCategories(category);
    }


    @Override
    @Transactional(rollbackOn = Exception.class)
    public VocabularyResponse createVocabularyFromExcel(VocabularyRequest request) {
        List<Vocabulary> savedVocabularies = new ArrayList<>();
        Set<String> categorySet = new HashSet<>();

        try (Workbook workbook = new XSSFWorkbook(request.getFile().getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);

            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue; 

                String koreanWord = getCellValue(row.getCell(1));
                String translation = getCellValue(row.getCell(2));
                String romanization = getCellValue(row.getCell(3));
                String categoryStr = getCellValue(row.getCell(4));

                if (koreanWord == null || translation == null || categoryStr == null) continue;

                VocabularyCategories category;
                try {
                    category = VocabularyCategories.valueOf(categoryStr.toUpperCase());
                    categorySet.add(category.name());
                } catch (IllegalArgumentException e) {
                    continue; 
                }

                Vocabulary vocab = new Vocabulary();
                vocab.setKoreanWord(koreanWord);
                vocab.setTranslation(translation);
                vocab.setRomanization(romanization);
                vocab.setVocabularyCategories(category);

                savedVocabularies.add(vocab);
            }

            vocabularyRepository.saveAll(savedVocabularies);
        } catch (IOException e) {
            throw new RuntimeException("Failed to parse Excel file: " + e.getMessage());
        }

        return VocabularyResponse.builder()
                .fileName(request.getFile().getOriginalFilename())
                .uploadedCount(savedVocabularies.size())
                .categories(new ArrayList<>(categorySet))
                .build();
    }

    
    private String getCellValue(Cell cell) {
        return (cell == null) ? null : cell.getStringCellValue();
    }


    @Override
    @Transactional
    public void deleteVocabularyById(UUID id) {
    if (!vocabularyRepository.existsById(id)) {
        throw new RuntimeException("Vocabulary with ID " + id + " not found");
    }
    vocabularyRepository.deleteById(id);
}
    
}
