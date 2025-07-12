package com.enigma.tekor.controller;

import com.enigma.tekor.exception.NotFoundException;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/download")
@RequiredArgsConstructor
@Tag(name = "File Download", description = "API for downloading files")
public class FileDownloadController {

    private final ResourceLoader resourceLoader;

    @GetMapping("/test-package-template")
    public ResponseEntity<Resource> downloadPackageTemplate() {
        String fileName = "TestPackage-Template.xlsx";
        Resource resource = resourceLoader.getResource("classpath:" + fileName);

        if (!resource.exists() || !resource.isReadable()) {
            throw new NotFoundException("File not found: " + fileName);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"");

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(resource);
    }

    @GetMapping("/vocabularies-template")
    public ResponseEntity<Resource> downloadVocabTemplate() {
        String fileName = "Vocabularies-Template.xlsx";
        Resource resource = resourceLoader.getResource("classpath:" + fileName);

        if (!resource.exists() || !resource.isReadable()) {
            throw new NotFoundException("File not found: " + fileName);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"");

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(resource);
    }

}
