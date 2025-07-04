package com.enigma.tekor.service;

import com.enigma.tekor.dto.request.ForgotPasswordRequest;
import com.enigma.tekor.dto.request.LoginRequest;
import com.enigma.tekor.dto.request.RegisterRequest;
import com.enigma.tekor.dto.request.ResetPasswordRequest;
import com.enigma.tekor.dto.response.LoginResponse;
import com.enigma.tekor.dto.response.UserResponse;

public interface AuthService {
    UserResponse register(RegisterRequest registerRequest);
    LoginResponse login (LoginRequest loginRequest);
    void verifyEmail(String token);
    void requestPasswordReset(ForgotPasswordRequest forgotPasswordRequest);
    void resetPassword(ResetPasswordRequest resetPasswordRequest);
    void validatePasswordResetToken(String token);
}
