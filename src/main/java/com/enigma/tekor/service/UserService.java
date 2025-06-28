package com.enigma.tekor.service;


import org.springframework.web.multipart.MultipartFile;
import com.enigma.tekor.dto.request.UpdateProfileRequest;
import com.enigma.tekor.dto.response.ProfilePictureResponse;
import com.enigma.tekor.dto.response.ProfileResponse;

public interface UserService {
    
    ProfileResponse getProfileById(String userId);
    ProfileResponse updateProfile(String userId, UpdateProfileRequest request);
    String getUserIdByUsername(String username);
    ProfilePictureResponse updateProfilePicture(String userId, MultipartFile file);
    
}
