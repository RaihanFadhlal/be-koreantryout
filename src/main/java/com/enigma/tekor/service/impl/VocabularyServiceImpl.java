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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



@Service
@RequiredArgsConstructor
public class VocabularyServiceImpl implements VocabularyService {
    private static final Logger logger = LoggerFactory.getLogger(VocabularyServiceImpl.class);
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

                String koreanWord = getCellValue(row.getCell(0));
                String translation = getCellValue(row.getCell(1));
                String romanization = getCellValue(row.getCell(2));
                String categoryStr = getCellValue(row.getCell(3));

                VocabularyCategories category;
                try {
                    category = VocabularyCategories.valueOf(categoryStr.toUpperCase());
                    categorySet.add(category.name());
                } catch (IllegalArgumentException e) {
                    logger.error("Invalid category '{}' in row {}. Skipping row. Error: {}", categoryStr, row.getRowNum(), e.getMessage());
                    continue;
                }

                Vocabulary vocab = new Vocabulary();
                vocab.setKoreanWord(koreanWord);
                vocab.setTranslation(translation);
                vocab.setRomanization(romanization);
                vocab.setVocabularyCategories(category);

                savedVocabularies.add(vocab);
                logger.debug("Added vocabulary to list: {}", vocab.getKoreanWord());
            }

            if (!savedVocabularies.isEmpty()) {
                vocabularyRepository.saveAll(savedVocabularies);
                logger.info("Successfully saved {} vocabularies.", savedVocabularies.size());
            } else {
                logger.warn("No valid vocabularies found to save from the Excel file.");
            }
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
        if (cell == null) {
            return null;
        }
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                return String.valueOf((int) cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return null;
        }
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