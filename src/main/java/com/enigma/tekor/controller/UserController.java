package com.enigma.tekor.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.enigma.tekor.dto.request.ChangePasswordRequest;
import com.enigma.tekor.dto.request.SearchUserRequest;
import com.enigma.tekor.dto.request.UpdateProfileRequest;
import com.enigma.tekor.dto.response.AdminUserDetailResponse;
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
        public ResponseEntity<CommonResponse<ProfileResponse>> getMyProfile(
                        @RequestHeader("Authorization") String token) {

                String userId = jwtUtil.getUserInfoByToken(token.replace("Bearer ", "")).get("userId");
                ProfileResponse profile = userService.getProfileById(UUID.fromString(userId));

                return ResponseEntity.ok(CommonResponse.<ProfileResponse>builder()
                                .status(HttpStatus.OK.getReasonPhrase())
                                .message("Successfully get profile")
                                .data(profile)
                                .build());
        }

        @PatchMapping
        @PreAuthorize("hasRole('USER')")
        public ResponseEntity<CommonResponse<ProfileResponse>> updateProfile(
                        @Valid @RequestBody UpdateProfileRequest request) {

                ProfileResponse updatedProfile = userService.updateProfile(
                                getCurrentUserId(),
                                request);

                return ResponseEntity.ok(CommonResponse.<ProfileResponse>builder()
                                .status(HttpStatus.OK.getReasonPhrase())
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
                                                .status(HttpStatus.OK.getReasonPhrase())
                                                .message("Profile picture updated successfully")
                                                .data(response)
                                                .build());
        }

        @PreAuthorize("hasRole('ADMIN')")
        private UUID getCurrentUserId() {
                String username = SecurityContextHolder.getContext().getAuthentication().getName();
                return userService.getUserIdByUsername(username);
        }

        @PostMapping(value = "/change-password", consumes = MediaType.APPLICATION_JSON_VALUE)
        @PreAuthorize("hasRole('USER')")
        public ResponseEntity<CommonResponse<String>> changePassword(
                        @Valid @RequestBody ChangePasswordRequest request) {
                userService.changePassword(getCurrentUserId(), request);
                return ResponseEntity.ok(CommonResponse.<String>builder()
                                .status(HttpStatus.OK.getReasonPhrase())
                                .message("Password updated successfully.")
                                .build());
        }

        @GetMapping("/all")
        @PreAuthorize("hasRole('ADMIN')")
        public ResponseEntity<CommonResponse<List<UserResponse>>> getAllUsers(
                @RequestParam(name = "page", defaultValue = "1") Integer page,
                @RequestParam(name = "size", defaultValue = "10") Integer size,
                @RequestParam(name = "username", required = false) String username
        ) {
                SearchUserRequest request = SearchUserRequest.builder()
                        .page(page)
                        .size(size)
                        .username(username)
                        .build();
                Page<UserResponse> users = userService.getAllUsers(request);
                return ResponseEntity.ok(CommonResponse.<List<UserResponse>>builder()
                                .status(HttpStatus.OK.getReasonPhrase())
                                .message("Successfully retrieved all users")
                                .data(users.getContent())
                                .build());
        }

        @GetMapping("/{id}")
        @PreAuthorize("hasRole('ADMIN')")
        public ResponseEntity<CommonResponse<AdminUserDetailResponse>> getUserById(@PathVariable String id) {
                AdminUserDetailResponse user = userService.getUserDetailForAdmin(id);
                return ResponseEntity.ok(CommonResponse.<AdminUserDetailResponse>builder()
                                .status(HttpStatus.OK.getReasonPhrase())
                                .message("Successfully retrieved user by id")
                                .data(user)
                                .build());
        }
}
