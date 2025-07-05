package com.enigma.tekor.controller;

import java.net.URI;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
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
import com.enigma.tekor.dto.request.RefreshTokenRequest;
import com.enigma.tekor.dto.request.RegisterRequest;
import com.enigma.tekor.dto.request.ResetPasswordRequest;
import com.enigma.tekor.dto.response.CommonResponse;
import com.enigma.tekor.dto.response.LoginResponse;
import com.enigma.tekor.dto.response.TokenResponse;
import com.enigma.tekor.dto.response.UserResponse;
import com.enigma.tekor.service.AuthService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    @Value("${app.frontend.verification-handler-url}")
    private String verificationHandlerUrl;

    @PostMapping("/register")
    public ResponseEntity<CommonResponse<UserResponse>> registerUser(@Valid @RequestBody RegisterRequest request) {
        UserResponse userResponse = authService.register(request);

        CommonResponse<UserResponse> response = CommonResponse.<UserResponse>builder()
                .status(HttpStatus.CREATED.getReasonPhrase())
                .message("Registration successful. Please check your email for verification.")
                .data(userResponse)
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<CommonResponse<LoginResponse>> loginUser(@RequestBody LoginRequest request) {
        LoginResponse loginResponse = authService.login(request);

        CommonResponse<LoginResponse> response = CommonResponse.<LoginResponse>builder()
                .status(HttpStatus.OK.getReasonPhrase())
                .message("Login successful.")
                .data(loginResponse)
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/verify")
    public ResponseEntity<Void> verifyEmail(@RequestParam("token") String token) {
        authService.verifyEmail(token);

        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(verificationHandlerUrl + "?status=verified"))
                .build();
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

    @GetMapping("/reset-password")
    public ResponseEntity<Void> showResetPasswordPage(@RequestParam("token") String token) {
        authService.validatePasswordResetToken(token);

        String resetUrl = frontendUrl + "/reset-password?token=" + token;

        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(resetUrl))
                .build();
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
