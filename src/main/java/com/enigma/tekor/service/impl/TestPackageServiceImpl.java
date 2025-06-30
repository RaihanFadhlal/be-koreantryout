package com.enigma.tekor.service.impl;

import com.enigma.tekor.constant.QuestionType;
import com.enigma.tekor.dto.request.CreateQuestionRequest;
import com.enigma.tekor.dto.request.CreateTestPackageRequest;
import com.enigma.tekor.entity.Question;
import com.enigma.tekor.entity.TestPackage;
import com.enigma.tekor.repository.TestPackageRepository;
import com.enigma.tekor.service.QuestionService;
import com.enigma.tekor.service.TestPackageService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TestPackageServiceImpl implements TestPackageService {

    private final TestPackageRepository testPackageRepository;
    private final QuestionService questionService;

    @Override
    @Transactional(rollbackOn = Exception.class)
    public void createTestPackageFromExcel(CreateTestPackageRequest request) {
        List<Question> questionsForPackage = new ArrayList<>();

        try (Workbook workbook = new XSSFWorkbook(request.getFile().getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);

            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue; // Skip header row

                String questionText = row.getCell(0).getStringCellValue();
                QuestionType questionType = QuestionType.valueOf(row.getCell(1).getStringCellValue().toUpperCase());
                String imageUrl = row.getCell(2) != null ? row.getCell(2).getStringCellValue() : null;
                String audioUrl = row.getCell(3) != null ? row.getCell(3).getStringCellValue() : null;
                String option1 = row.getCell(4).getStringCellValue();
                String option2 = row.getCell(5).getStringCellValue();
                String option3 = row.getCell(6).getStringCellValue();
                String option4 = row.getCell(7).getStringCellValue();
                String correctOption = row.getCell(8).getStringCellValue();

                List<String> options = new ArrayList<>();
                options.add(option1);
                options.add(option2);
                options.add(option3);
                options.add(option4);

                CreateQuestionRequest questionRequest = new CreateQuestionRequest(
                        questionText,
                        questionType,
                        imageUrl,
                        audioUrl,
                        options,
                        correctOption
                );

                Question savedQuestion = questionService.createQuestionWithOptions(questionRequest);
                questionsForPackage.add(savedQuestion);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to parse Excel file: " + e.getMessage());
        }

        TestPackage testPackage = new TestPackage();
        testPackage.setName(request.getName());
        testPackage.setDescription(request.getDescription());
        testPackage.setPrice(request.getPrice());
        testPackage.setQuestions(questionsForPackage);

        testPackageRepository.save(testPackage);
    }
}
