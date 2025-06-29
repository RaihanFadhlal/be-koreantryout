package com.enigma.tekor.service;

import java.util.Map;

import org.springframework.web.multipart.MultipartFile;

public interface CloudinaryService {
    Map<?, ?> upload(MultipartFile multipartFile) throws Exception;
    void delete(String publicId) throws Exception;
}
