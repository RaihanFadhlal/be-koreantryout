package com.enigma.tekor.service.impl;

import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.enigma.tekor.dto.request.ChangePasswordRequest;
import com.enigma.tekor.dto.request.UpdateProfileRequest;
import com.enigma.tekor.dto.response.ProfilePictureResponse;
import com.enigma.tekor.dto.response.ProfileResponse;
import com.enigma.tekor.dto.response.UserResponse;
import com.enigma.tekor.entity.User;
import com.enigma.tekor.exception.BadRequestException;
import com.enigma.tekor.exception.InvalidFileException;
import com.enigma.tekor.exception.UserNotFoundException;
import com.enigma.tekor.repository.UserRepository;
import com.enigma.tekor.service.CloudinaryService;
import com.enigma.tekor.service.UserService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CloudinaryService cloudinaryService;

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Override
    public ProfileResponse getProfileById(String userId) {
        User user = userRepository.findById(UUID.fromString(userId))
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        return convertToProfileResponse(user);
    }

    @Override
    public ProfileResponse updateProfile(String userId, UpdateProfileRequest request) {
        User user = userRepository.findById(UUID.fromString(userId))
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        user.setFullName(request.getFullName());

        User updatedUser = userRepository.save(user);
        return convertToProfileResponse(updatedUser);
    }

    @Override
    public String getUserIdByUsername(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email))
                .getId().toString();
    }

    @Override
    public ProfilePictureResponse updateProfilePicture(String userId, MultipartFile file) {
        validateImageFile(file);

        try {
            User user = userRepository.findById(UUID.fromString(userId))
                    .orElseThrow(() -> new UserNotFoundException("User not found"));

            if (user.getImageUrl() != null && !user.getImageUrl().isEmpty()) {
                try {
                    String publicId = cloudinaryService.extractPublicIdFromUrl(user.getImageUrl());
                    cloudinaryService.delete(publicId);
                } catch (Exception e) {
                    System.err.println(e.getMessage());
                }
            }

            Map<?, ?> uploadResult = cloudinaryService.upload(file);

            String newImageUrl = (String) uploadResult.get("secure_url");

            user.setImageUrl(newImageUrl);
            userRepository.save(user);

            return new ProfilePictureResponse(newImageUrl);

        } catch (Exception e) {
            throw new BadRequestException("Failed to update profile picture: " + e.getMessage());
        }
    }

    @Override
    public void changePassword(String userId, ChangePasswordRequest request) {
        User user = userRepository.findById(UUID.fromString(userId))
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BadRequestException("Invalid current password");
        }

        if (!request.getNewPassword().equals(request.getConfirmNewPassword())) {
            throw new BadRequestException("New password and confirmation password do not match");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    private ProfileResponse convertToProfileResponse(User user) {
        return ProfileResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .username(user.getUsername())
                .email(user.getEmail())
                .imageUrl(user.getImageUrl())
                .isVerified(user.getIsVerified())
                .createdAt(user.getCreatedAt().toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDateTime())
                .build();
    }

    private void validateImageFile(MultipartFile file) {
        if (file.isEmpty())
            throw new InvalidFileException("File cannot be empty");

        String contentType = file.getContentType();
        if (!"image/jpeg".equals(contentType) && !"image/png".equals(contentType)) {
            throw new InvalidFileException("Only JPEG/PNG images are allowed");
        }
    }

    @Override
    public User getByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElse(null);
    }

    @Override
    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElse(null);
    }

    @Override
    public User save(User user) {
        return userRepository.save(user);
    }

    @Override
    public User findById(String id) {
        return userRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + id));
    }

    @Override
    public User update(User user) {
        return userRepository.save(user);
    }


    @Override
    public List<UserResponse> getAllUsers() {
    return userRepository.findAll().stream()
            .map(user -> UserResponse.builder()
                    .id(user.getId())  
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .build())
            .toList();
    }
}
