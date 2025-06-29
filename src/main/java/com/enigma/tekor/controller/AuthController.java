package com.enigma.tekor.controller;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.enigma.tekor.dto.request.ForgotPasswordRequest;
import com.enigma.tekor.dto.request.LoginRequest;
import com.enigma.tekor.dto.request.RegisterRequest;
import com.enigma.tekor.dto.request.ResetPasswordRequest;
import com.enigma.tekor.dto.response.CommonResponse;
import com.enigma.tekor.dto.response.LoginResponse;
import com.enigma.tekor.dto.response.UserResponse;
import com.enigma.tekor.service.AuthService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;


    @PostMapping("/register")
    public ResponseEntity<CommonResponse<UserResponse>> registerUser(@RequestBody RegisterRequest request) {
        UserResponse userResponse = authService.register(request);
        
        CommonResponse<UserResponse> response = CommonResponse.<UserResponse>builder()
                .status("success")
                .message("Registration successful. Please check your email for verification.")
                .data(userResponse)
                .build();
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<CommonResponse<LoginResponse>> loginUser(@RequestBody LoginRequest request) {
        LoginResponse loginResponse = authService.login(request);

        CommonResponse<LoginResponse> response = CommonResponse.<LoginResponse>builder()
                .status("success")
                .message("Login successful.")
                .data(loginResponse)
                .build();
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/verify")
    public ResponseEntity<CommonResponse<String>> verifyEmail(@RequestParam("userId") UUID userId) {
        String message = authService.verifyEmail(userId);

        CommonResponse<String> response = CommonResponse.<String>builder()
                .status("success")
                .message(message)
                .data(null)
                .build();

        return ResponseEntity.ok(response);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<CommonResponse<?>> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        authService.requestPasswordReset(request);
        
        CommonResponse<?> response = CommonResponse.builder()
                .status(HttpStatus.OK.getReasonPhrase())
                .message("If the email is registered, a password reset link has been sent.")
                .build();
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/reset-password")
    public ResponseEntity<CommonResponse<?>> resetPassword(@RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        
        CommonResponse<?> response = CommonResponse.builder()
                .status(HttpStatus.OK.getReasonPhrase())
                .message("Your password has been successfully reset. Please log in.")
                .build();
        
        return ResponseEntity.ok(response);
    }
}
