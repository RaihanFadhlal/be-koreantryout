package com.enigma.tekor.service.impl;

import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.enigma.tekor.service.CloudinaryService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CloudinaryServiceImpl implements CloudinaryService {
    private final Cloudinary cloudinary;

    @Override
    public Map<?, ?> upload(MultipartFile file) throws Exception {
        return cloudinary.uploader().upload(file.getBytes(), ObjectUtils.emptyMap());
    }

    @Override
    public void delete(String publicId) throws Exception {
        cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
    }

    @Override
    public String extractPublicIdFromUrl(String imageUrl) {
        int startIndex = imageUrl.lastIndexOf('/') + 1;
        int endIndex = imageUrl.lastIndexOf('.');
        if (startIndex != -1 && endIndex != -1 && endIndex > startIndex) {
            return imageUrl.substring(startIndex, endIndex);
        }
        return null;
    }
}
