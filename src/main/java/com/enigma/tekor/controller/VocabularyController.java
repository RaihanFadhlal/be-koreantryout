package com.enigma.tekor.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.enigma.tekor.constant.VocabularyCategories;
import com.enigma.tekor.dto.request.VocabularyRequest;
import com.enigma.tekor.dto.response.CommonResponse;
import com.enigma.tekor.dto.response.VocabularyResponse;
import com.enigma.tekor.entity.Vocabulary;
import com.enigma.tekor.service.VocabularyService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/vocabularies")
@RequiredArgsConstructor
public class VocabularyController {

    private final VocabularyService vocabularyService;


    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CommonResponse<VocabularyResponse>> createVocabulary(
        @ModelAttribute VocabularyRequest request
) {
    VocabularyResponse response = vocabularyService.createVocabularyFromExcel(request);

    return ResponseEntity.status(HttpStatus.CREATED).body(CommonResponse.<VocabularyResponse>builder()
            .status(HttpStatus.CREATED.name())
            .message("Vocabulary data uploaded successfully")
            .data(response)
            .build());
}

    @GetMapping("/categories")
    public ResponseEntity<CommonResponse<List<String>>> getAllCategories() {
        List<String> categories = vocabularyService.getAllCategoryNames();
        return ResponseEntity.ok(
                CommonResponse.<List<String>>builder()
                        .status(HttpStatus.OK.name())
                        .message("Categories retrieved successfully")
                        .data(categories)
                        .build());
    }

    @GetMapping
    public ResponseEntity<CommonResponse<?>> getVocabularies(
            @RequestParam String category,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size
    ) {
    try {
        VocabularyCategories categoryEnum = VocabularyCategories.valueOf(category.toUpperCase());
        Page<Vocabulary> vocabularies = vocabularyService.getVocabulariesByCategory(categoryEnum, page, size);

        return ResponseEntity.ok(
                CommonResponse.<Page<Vocabulary>>builder()
                        .status(HttpStatus.OK.name())
                        .message("Vocabularies retrieved successfully for category: " + category)
                        .data(vocabularies)
                        .build());
    } catch (IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(
                CommonResponse.builder()
                        .status(HttpStatus.BAD_REQUEST.name())
                        .message("Invalid category: " + category)
                        .build());
    }
}


    @DeleteMapping("/{id}")
    public ResponseEntity<CommonResponse<String>> deleteVocabulary(@PathVariable UUID id) {
    vocabularyService.deleteVocabularyById(id);
    return ResponseEntity.ok(
        CommonResponse.<String>builder()
            .status(HttpStatus.OK.name())
            .message("Vocabulary deleted successfully")
            .data("Deleted ID: " + id)
            .build()
    );
}
}