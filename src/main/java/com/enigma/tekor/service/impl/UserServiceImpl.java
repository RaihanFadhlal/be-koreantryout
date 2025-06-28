package com.enigma.tekor.service.impl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.ZoneId;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.enigma.tekor.dto.request.ChangePasswordRequest;
import com.enigma.tekor.dto.request.UpdateProfileRequest;
import com.enigma.tekor.dto.response.ProfilePictureResponse;
import com.enigma.tekor.dto.response.ProfileResponse;
import com.enigma.tekor.entity.User;
import com.enigma.tekor.exception.BadRequestException;
import com.enigma.tekor.exception.FileStorageException;
import com.enigma.tekor.exception.InvalidFileException;
import com.enigma.tekor.exception.UserNotFoundException;
import com.enigma.tekor.exception.UsernameAlreadyExistsException;
import com.enigma.tekor.repository.UserRepository;
import com.enigma.tekor.service.UserService;
import lombok.RequiredArgsConstructor;



@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
       
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

       
        if (!user.getUsername().equals(request.getUsername())) {
            userRepository.findByUsername(request.getUsername())
                    .ifPresent(u -> {
                        throw new UsernameAlreadyExistsException("Username already taken");
                    });
        }

        user.setFullName(request.getFullName());
        user.setUsername(request.getUsername());

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

      
        String fileName = "user_" + userId + "_" + System.currentTimeMillis() +
                getFileExtension(file.getOriginalFilename());

        
        String relativePath = "/profile-pictures/" + fileName;
        Path destination = Paths.get(uploadDir + relativePath);

        try {
            Files.createDirectories(destination.getParent());
            Files.copy(file.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);

            
            User user = userRepository.findById(UUID.fromString(userId))
                    .orElseThrow(() -> new UserNotFoundException("User not found"));

            
            if (user.getImageUrl() != null) {
                Files.deleteIfExists(Paths.get(uploadDir + user.getImageUrl()));
            }

            user.setImageUrl(relativePath);
            userRepository.save(user);

            return new ProfilePictureResponse(relativePath);

        } catch (IOException e) {
            throw new FileStorageException("Failed to store file: " + e.getMessage());
        }
    }

    @Override
    public void changePassword(String userId, ChangePasswordRequest request) {
        User user = userRepository.findById(UUID.fromString(userId))
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BadRequestException("Invalid current password");
        }

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
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

    
    private String getFileExtension(String filename) {
        return filename.substring(filename.lastIndexOf("."));
    }
    

    
}
