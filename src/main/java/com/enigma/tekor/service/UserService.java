package com.enigma.tekor.service;


import org.springframework.web.multipart.MultipartFile;

import com.enigma.tekor.dto.request.ChangePasswordRequest;
import com.enigma.tekor.dto.request.UpdateProfileRequest;
import com.enigma.tekor.dto.response.ProfilePictureResponse;
import com.enigma.tekor.dto.response.ProfileResponse;
import com.enigma.tekor.entity.User;

public interface UserService {
    ProfileResponse getProfileById(String userId);
    ProfileResponse updateProfile(String userId, UpdateProfileRequest request);
    String getUserIdByUsername(String username);
    ProfilePictureResponse updateProfilePicture(String userId, MultipartFile file);
    void changePassword(String userId, ChangePasswordRequest request);
    User getByEmail(String email);
    User findByUsername(String username);
    User save(User user);
    User findById(String id);
    User update(User user);
}
