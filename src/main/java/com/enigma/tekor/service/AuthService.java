package com.enigma.tekor.service;

import java.util.UUID;

import com.enigma.tekor.dto.request.LoginRequest;
import com.enigma.tekor.dto.request.RegisterRequest;
import com.enigma.tekor.dto.response.LoginResponse;
import com.enigma.tekor.dto.response.UserResponse;

public interface AuthService {
    UserResponse register(RegisterRequest registerRequest);
    LoginResponse login (LoginRequest loginRequest);
    String verifyEmail(UUID userId);
}
