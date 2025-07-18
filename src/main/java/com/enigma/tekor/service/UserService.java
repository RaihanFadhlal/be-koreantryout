package com.enigma.tekor.service;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import com.enigma.tekor.dto.request.ChangePasswordRequest;
import com.enigma.tekor.dto.request.SearchUserRequest;
import com.enigma.tekor.dto.request.UpdateProfileRequest;
import com.enigma.tekor.dto.response.AdminUserDetailResponse;
import com.enigma.tekor.dto.response.ProfilePictureResponse;
import com.enigma.tekor.dto.response.ProfileResponse;
import com.enigma.tekor.dto.response.UserResponse;
import com.enigma.tekor.entity.User;

public interface UserService {
    ProfileResponse getProfileById(UUID userId);
    ProfileResponse updateProfile(UUID userId, UpdateProfileRequest request);
    UUID getUserIdByUsername(String username);
    ProfilePictureResponse updateProfilePicture(UUID userId, MultipartFile file);
    void changePassword(UUID userId, ChangePasswordRequest request);
    User getByEmail(String email);
    User findByUsername(String username);
    User save(User user);
    User findById(UUID id);
    User update(User user);
    Page<UserResponse> getAllUsers(SearchUserRequest request);
    AdminUserDetailResponse getUserDetailForAdmin(String id);
}
