package com.enigma.tekor.service.impl;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.enigma.tekor.exception.BadRequestException;
import com.enigma.tekor.exception.FileStorageException;
import com.enigma.tekor.exception.NotFoundException;
import com.enigma.tekor.service.CloudinaryService;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.enigma.tekor.constant.QuestionType;
import com.enigma.tekor.dto.request.CreateQuestionRequest;
import com.enigma.tekor.dto.request.CreateTestPackageRequest;
import com.enigma.tekor.dto.request.UpdateTestPackageRequest;
import com.enigma.tekor.dto.response.BundleResponse;
import com.enigma.tekor.dto.response.ProductResponse;
import com.enigma.tekor.dto.response.TestPackageResponse;
import com.enigma.tekor.entity.Question;
import com.enigma.tekor.entity.TestPackage;
import com.enigma.tekor.repository.QuestionRepository;
import com.enigma.tekor.repository.TestPackageRepository;
import com.enigma.tekor.service.BundleService;
import com.enigma.tekor.service.QuestionService;
import com.enigma.tekor.service.TestPackageService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TestPackageServiceImpl implements TestPackageService {

    private final TestPackageRepository testPackageRepository;
    private final QuestionService questionService;
    private final BundleService bundleService;
    private final QuestionRepository questionRepository;
    private final CloudinaryService cloudinaryService;

    @Override
    @Transactional(rollbackOn = Exception.class)
    public TestPackage createTestPackageFromExcel(CreateTestPackageRequest request) {
        List<Question> questionsForPackage = new ArrayList<>();

        try (Workbook workbook = new XSSFWorkbook(request.getFile().getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            Integer number = 1;
            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue;

                if (isRowEmpty(row)) {
                    continue;
                }

                String questionText = getCellValue(row.getCell(0));

                if (questionText == null || questionText.trim().isEmpty()) {
                    questionText = " ";
                }

                try {
                    QuestionType questionType = QuestionType.valueOf(getCellValue(row.getCell(1)).trim().toUpperCase());
                    String imageUrl = getCellValue(row.getCell(2));
                    String audioUrl = getCellValue(row.getCell(3));
                    String option1 = getCellValue(row.getCell(4));
                    String option2 = getCellValue(row.getCell(5));
                    String option3 = getCellValue(row.getCell(6));
                    String option4 = getCellValue(row.getCell(7));
                    String correctOption = getCellValue(row.getCell(8));
                    String desc = getCellValue(row.getCell(9));

                    List<String> options = new ArrayList<>();
                    options.add(option1);
                    options.add(option2);
                    options.add(option3);
                    options.add(option4);

                    CreateQuestionRequest questionRequest = new CreateQuestionRequest(
                            questionText,
                            desc,
                            questionType,
                            imageUrl,
                            audioUrl,
                            number,
                            options,
                            correctOption
                    );

                    Question savedQuestion = questionService.createQuestionWithOptions(questionRequest);
                    questionsForPackage.add(savedQuestion);
                    number++;
                } catch (Exception e) {
                    System.err.println("Error processing row " + (row.getRowNum() + 1) + ": " + e.getMessage());
                    continue;
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to parse Excel file: " + e.getMessage());
        }

        TestPackage testPackage = new TestPackage();
        testPackage.setName(request.getName());
        testPackage.setDescription(request.getDescription());
        testPackage.setImageUrl(request.getImageUrl());
        testPackage.setPrice(request.getPrice());
        testPackage.setDiscountPrice(request.getDiscountPrice());
        testPackage.setIsTrial(request.getPrice().compareTo(BigDecimal.ZERO) <= 0);
        testPackage.setQuestions(questionsForPackage);

        return testPackageRepository.save(testPackage);
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public TestPackageResponse update(String id, UpdateTestPackageRequest request) {
        try {
            TestPackage testPackage = testPackageRepository.findById(UUID.fromString(id))
                    .orElseThrow(() -> new NotFoundException("Test package not found"));

            testPackage.setName(request.getName());
            testPackage.setDescription(request.getDescription());
            testPackage.setPrice(BigDecimal.valueOf(request.getPrice()));
            testPackage.setDiscountPrice(BigDecimal.valueOf(request.getDiscountPrice()));

            if (request.getImage() != null && !request.getImage().isEmpty()) {
                if (testPackage.getImageUrl() != null && !testPackage.getImageUrl().isEmpty()) {
                    try {
                        String publicId = cloudinaryService.extractPublicIdFromUrl(testPackage.getImageUrl());
                        cloudinaryService.delete(publicId);
                    } catch (Exception e) {
                        throw new FileStorageException("Failed to delete existing image from Cloudinary", e);
                    }
                }
                try {
                    Map<?, ?> uploadResult = cloudinaryService.upload(request.getImage());
                    String newImageUrl = (String) uploadResult.get("secure_url");
                    testPackage.setImageUrl(newImageUrl);
                } catch (Exception e) {
                    throw new FileStorageException("Failed to upload new image to Cloudinary", e);
                }
            }

            testPackageRepository.save(testPackage);

            return TestPackageResponse.builder()
                    .id(String.valueOf(testPackage.getId()))
                    .name(testPackage.getName())
                    .description(testPackage.getDescription())
                    .imageUrl(testPackage.getImageUrl())
                    .price(testPackage.getPrice().doubleValue())
                    .discountPrice(testPackage.getDiscountPrice().doubleValue())
                    .build();
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid UUID format for id: " + id);
        }
    }

    @Override
    public TestPackageResponse getById(String id) {
        TestPackage testPackage = testPackageRepository.findById(UUID.fromString(id)).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Test package not found"));
        return TestPackageResponse.builder()
                .id(String.valueOf(testPackage.getId()))
                .name(testPackage.getName())
                .description(testPackage.getDescription())
                .imageUrl(testPackage.getImageUrl())
                .price(testPackage.getPrice().doubleValue())
                .discountPrice(testPackage.getDiscountPrice().doubleValue())
                .build();
    }

    @Override
    public TestPackage getOneById(UUID id) {
        return testPackageRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Test package not found"));
    }

    @Override
    public void delete(String id) {
        TestPackage testPackage = testPackageRepository.findById(UUID.fromString(id)).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Test package not found"));
        testPackageRepository.delete(testPackage);
    }

    @Override
    public List<ProductResponse> getAllPackagesAndBundles() {
        List<TestPackage> testPackages = testPackageRepository.findAll();
        List<ProductResponse> productResponses = testPackages.stream()
                .map(tp -> ProductResponse.builder()
                        .id(tp.getId())
                        .name(tp.getName())
                        .description(tp.getDescription())
                        .imageUrl(tp.getImageUrl())
                        .price(tp.getPrice())
                        .discountPrice(tp.getDiscountPrice())
                        .type("package")
                        .build())
                .toList();

        List<BundleResponse> bundleResponses = bundleService.getAll();
        List<ProductResponse> bundleProductResponses = bundleResponses.stream()
                .map(br -> ProductResponse.builder()
                        .id(br.getId())
                        .name(br.getName())
                        .description(br.getDescription())
                        .imageUrl(br.getImageUrl())
                        .price(br.getPrice())
                        .discountPrice(br.getDiscountPrice())
                        .type("bundle")
                        .build())
                .toList();

        return Stream.concat(productResponses.stream(), bundleProductResponses.stream())
                .collect(Collectors.toList());
    }

    @Override
    public List<TestPackageResponse> getAllTestPackages() {
        List<TestPackage> testPackages = testPackageRepository.findAll();
        return testPackages.stream().map(testPackage -> TestPackageResponse.builder()
                .id(String.valueOf(testPackage.getId()))
                .name(testPackage.getName())
                .description(testPackage.getDescription())
                .imageUrl(testPackage.getImageUrl())
                .price(testPackage.getPrice().doubleValue())
                .discountPrice(testPackage.getDiscountPrice().doubleValue())
                .build()).collect(Collectors.toList());
    }

    @Override
    public Integer getTotalQuestionsByPackageId(String packageId) {
        TestPackage testPackage = testPackageRepository.findById(UUID.fromString(packageId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Test package not found"));
        return questionRepository.countByTestPackagesContains(testPackage);
    }

    private String getCellValue(Cell cell) {
        if (cell == null) {
            return null;
        }
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> String.valueOf((int) cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> cell.getCellFormula();
            default -> null;
        };
    }

    private boolean isRowEmpty(Row row) {
        if (row == null) {
            return true;
        }
        if (row.getLastCellNum() <= 0) {
            return true;
        }
        for (int cellNum = row.getFirstCellNum(); cellNum < row.getLastCellNum(); cellNum++) {
            Cell cell = row.getCell(cellNum);
            if (cell != null && cell.getCellType() != CellType.BLANK &&
                    !cell.toString().trim().isEmpty()) {
                return false;
            }
        }
        return true;
    }
}

