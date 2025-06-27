package com.enigma.tekor.service.impl;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.enigma.tekor.dto.request.LoginRequest;
import com.enigma.tekor.dto.request.RegisterRequest;
import com.enigma.tekor.dto.response.LoginResponse;
import com.enigma.tekor.dto.response.TokenResponse;
import com.enigma.tekor.dto.response.UserLoginInfo;
import com.enigma.tekor.dto.response.UserResponse;
import com.enigma.tekor.entity.Role;
import com.enigma.tekor.entity.User;
import com.enigma.tekor.exception.BadRequestException;
import com.enigma.tekor.repository.UserRepository;
import com.enigma.tekor.service.AuthService;
import com.enigma.tekor.service.EmailService;
import com.enigma.tekor.service.RoleService;
import com.enigma.tekor.util.JwtUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final PasswordEncoder passwordEncoder;
    private final RoleService roleService;
    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final EmailService emailService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserResponse register(RegisterRequest registerRequest) {
        userRepository.findByUsername(registerRequest.getUsername()).ifPresent(user -> {
            throw new BadRequestException("Username already exists. Please try another username");
        });

        userRepository.findByEmail(registerRequest.getEmail()).ifPresent(user -> {
            throw new BadRequestException("Email already exists. Please try another email");
        });

        Role userRole = roleService.getOrSave("ROLE_USER");
        User newUser = User.builder()
                .fullName(registerRequest.getFullName())
                .username(registerRequest.getUsername())
                .email(registerRequest.getEmail())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .role(userRole)
                .isVerified(false)
                .build();

        userRepository.save(newUser);

        String verificationUrl = "http://localhost:8081/api/auth/verify?userId=" + newUser.getId();
        String emailBody = "<h1>Verifikasi Email - Aplikasi Tekor</h1>"
                + "<p>Terima kasih telah mendaftar. Silakan klik tautan di bawah ini untuk memverifikasi alamat email Anda:</p>"
                + "<a href=\"" + verificationUrl
                + "\" style=\"background-color:#008CBA;color:white;padding:15px 25px;text-align:center;text-decoration:none;display:inline-block;border-radius:8px;\">Verifikasi Email Saya</a>"
                + "<p>Jika Anda tidak merasa mendaftar, abaikan email ini.</p>";

        emailService.sendEmail(newUser.getEmail(), "Verifikasi Email - Aplikasi Tekor", emailBody);

        return UserResponse.builder()
                .id(newUser.getId())
                .fullName(newUser.getFullName())
                .username(newUser.getUsername())
                .email(newUser.getEmail())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()
                    )
            );

            User user = userRepository.findByUsername(loginRequest.getUsername())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Kredensial tidak valid"));

            if (Boolean.FALSE.equals(user.getIsVerified())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Akun belum diverifikasi. Silakan periksa email Anda.");
            }

            SecurityContextHolder.getContext().setAuthentication(authentication);

            String accessToken = jwtUtil.generateAccessToken(user);
            String refreshToken = jwtUtil.generateRefreshToken(user);

            UserLoginInfo userLoginInfo = UserLoginInfo.builder()
                    .id(user.getId())
                    .fullName(user.getFullName())
                    .role(user.getRole().getName())
                    .build();

            TokenResponse tokenResponse = TokenResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .build();

            return LoginResponse.builder()
                    .user(userLoginInfo)
                    .token(tokenResponse)
                    .build();
        } catch (AuthenticationException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Kredensial tidak valid");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String verifyEmail(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pengguna tidak ditemukan"));

        if (Boolean.TRUE.equals(user.getIsVerified())) {
            return "Akun Anda sudah pernah diverifikasi sebelumnya.";
        }
        
        user.setIsVerified(true);
        userRepository.save(user);
        
        return "Akun Anda telah berhasil diverifikasi! Sekarang Anda dapat melakukan login.";
    }

}
