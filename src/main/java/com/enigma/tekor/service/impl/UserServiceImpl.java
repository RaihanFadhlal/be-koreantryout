package com.enigma.tekor.service.impl;

import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.enigma.tekor.dto.request.ChangePasswordRequest;
import com.enigma.tekor.dto.request.SearchUserRequest;
import com.enigma.tekor.dto.request.UpdateProfileRequest;
import com.enigma.tekor.dto.response.AdminUserDetailResponse;
import com.enigma.tekor.dto.response.ProfilePictureResponse;
import com.enigma.tekor.dto.response.ProfileResponse;
import com.enigma.tekor.dto.response.TransactionSummaryResponse;
import com.enigma.tekor.dto.response.UserResponse;
import com.enigma.tekor.entity.Transaction;
import com.enigma.tekor.entity.User;
import com.enigma.tekor.exception.BadRequestException;
import com.enigma.tekor.exception.InvalidFileException;
import com.enigma.tekor.exception.UserNotFoundException;
import com.enigma.tekor.repository.UserRepository;
import com.enigma.tekor.service.CloudinaryService;
import com.enigma.tekor.service.TransactionService;
import com.enigma.tekor.service.UserService;
import com.enigma.tekor.specification.UserSpecification;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CloudinaryService cloudinaryService;
    private final TransactionService transactionService;

    @Value("${file.upload-dir}")
    private String uploadDir;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, CloudinaryService cloudinaryService, @Lazy TransactionService transactionService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.cloudinaryService = cloudinaryService;
        this.transactionService = transactionService;
    }

    @Override
    public ProfileResponse getProfileById(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        return convertToProfileResponse(user);
    }

    @Override
    public ProfileResponse updateProfile(UUID userId, UpdateProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        user.setFullName(request.getFullName());

        User updatedUser = userRepository.save(user);
        return convertToProfileResponse(updatedUser);
    }

    @Override
    public UUID getUserIdByUsername(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email))
                .getId();
    }

    @Override
    public ProfilePictureResponse updateProfilePicture(UUID userId, MultipartFile file) {
        validateImageFile(file);

        try {
            User user = userRepository.findById(userId)
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
    public void changePassword(UUID userId, ChangePasswordRequest request) {
        User user = userRepository.findById(userId)
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
    public User findById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + id));
    }

    @Override
    public User update(User user) {
        return userRepository.save(user);
    }

    @Override
    public Page<UserResponse> getAllUsers(SearchUserRequest request) {
        if (request.getPage() <= 0) request.setPage(1);
        Pageable pageable = PageRequest.of(request.getPage() - 1, request.getSize());
        Specification<User> specification = UserSpecification.getSpecification(request);
        Page<User> users = userRepository.findAll(specification, pageable);
        return users.map(user -> UserResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .username(user.getUsername())
                .email(user.getEmail())
                .build());
    }

    @Override
    public AdminUserDetailResponse getUserDetailForAdmin(String id) {
        User user = findById(UUID.fromString(id));
        List<Transaction> transactions = transactionService.findAllByUser(user);

        List<TransactionSummaryResponse> transactionSummaries = transactions.stream()
                .map(this::mapToTransactionSummary)
                .collect(Collectors.toList());

        return AdminUserDetailResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .username(user.getUsername())
                .email(user.getEmail())
                .imageUrl(user.getImageUrl())
                .isVerified(user.getIsVerified())
                .createdAt(user.getCreatedAt().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime())
                .transactions(transactionSummaries)
                .build();
    }

    private TransactionSummaryResponse mapToTransactionSummary(Transaction transaction) {
        String itemName = "Item not found";
        if (transaction.getTestPackage() != null) {
            itemName = transaction.getTestPackage().getName();
        } else if (transaction.getBundle() != null) {
            itemName = transaction.getBundle().getName();
        }

        return TransactionSummaryResponse.builder()
                .transactionId(transaction.getId().toString())
                .status(transaction.getStatus())
                .amount(transaction.getAmount())
                .purchasedItemName(itemName)
                .transactionDate(transaction.getCreatedAt())
                .build();
    }
}
