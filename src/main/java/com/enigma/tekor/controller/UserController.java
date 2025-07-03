package com.enigma.tekor.controller;

import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.enigma.tekor.dto.request.ChangePasswordRequest;
import com.enigma.tekor.dto.request.UpdateProfileRequest;
import com.enigma.tekor.dto.response.CommonResponse;
import com.enigma.tekor.dto.response.ProfilePictureResponse;
import com.enigma.tekor.dto.response.ProfileResponse;
import com.enigma.tekor.dto.response.UserResponse;
import com.enigma.tekor.service.UserService;
import com.enigma.tekor.util.JwtUtil;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

        private final UserService userService;
        private final JwtUtil jwtUtil;

        @GetMapping
        @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
        public ResponseEntity<ProfileResponse> getMyProfile(
                        @RequestHeader("Authorization") String token) {

                String userId = jwtUtil.getUserInfoByToken(token.replace("Bearer ", "")).get("userId");
                ProfileResponse profile = userService.getProfileById(userId);

                return ResponseEntity.ok(profile);
        }

        @PatchMapping
        @PreAuthorize("hasRole('USER')")
        public ResponseEntity<CommonResponse<ProfileResponse>> updateProfile(
                        @Valid @RequestBody UpdateProfileRequest request) {

                ProfileResponse updatedProfile = userService.updateProfile(
                                getCurrentUserId(),
                                request);

                return ResponseEntity.ok(CommonResponse.<ProfileResponse>builder()
                                .status("success")
                                .message("Profile updated successfully")
                                .data(updatedProfile)
                                .build());
        }

        @PostMapping(value = "/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
        @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
        public ResponseEntity<CommonResponse<ProfilePictureResponse>> updateProfilePicture(
                        @RequestParam("avatar") MultipartFile file) {

                ProfilePictureResponse response = userService.updateProfilePicture(
                                getCurrentUserId(),
                                file);

                return ResponseEntity.ok(
                                CommonResponse.<ProfilePictureResponse>builder()
                                                .status("success")
                                                .message("Profile picture updated successfully")
                                                .data(response)
                                                .build());
        }

        @PreAuthorize("hasRole('ADMIN')")
        private String getCurrentUserId() {
                String username = SecurityContextHolder.getContext().getAuthentication().getName();
                return userService.getUserIdByUsername(username);
        }

        @PostMapping(value = "/change-password", consumes = MediaType.APPLICATION_JSON_VALUE)
        @PreAuthorize("hasRole('USER')")
        public ResponseEntity<CommonResponse<String>> changePassword(
                        @Valid @RequestBody ChangePasswordRequest request) {
                userService.changePassword(getCurrentUserId(), request);
                return ResponseEntity.ok(CommonResponse.<String>builder()
                                .status("success")
                                .message("Password updated successfully.")
                                .build());
        }


         @GetMapping("/all")
        @PreAuthorize("hasRole('ADMIN')")
        public ResponseEntity<CommonResponse<List<UserResponse>>> getAllUsers() {
        List<UserResponse> users = userService.getAllUsers();
    
        return ResponseEntity.ok(CommonResponse.<List<UserResponse>>builder()
            .status("success")
            .message("Successfully retrieved all users")
            .data(users)
            .build());
}
}
