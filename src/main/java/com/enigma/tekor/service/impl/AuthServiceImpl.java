package com.enigma.tekor.service.impl;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
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

import com.enigma.tekor.dto.request.ForgotPasswordRequest;
import com.enigma.tekor.dto.request.LoginRequest;
import com.enigma.tekor.dto.request.RegisterRequest;
import com.enigma.tekor.dto.request.ResetPasswordRequest;
import com.enigma.tekor.dto.response.LoginResponse;
import com.enigma.tekor.dto.response.TokenResponse;
import com.enigma.tekor.dto.response.UserLoginInfo;
import com.enigma.tekor.dto.response.UserResponse;
import com.enigma.tekor.entity.PasswordResetToken;
import com.enigma.tekor.entity.Role;
import com.enigma.tekor.entity.User;
import com.enigma.tekor.exception.BadRequestException;
import com.enigma.tekor.repository.PasswordResetTokenRepository;
import com.enigma.tekor.security.CustomUserDetails;
import com.enigma.tekor.service.AuthService;
import com.enigma.tekor.service.EmailService;
import com.enigma.tekor.service.RoleService;
import com.enigma.tekor.service.UserService;
import com.enigma.tekor.util.JwtUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final PasswordEncoder passwordEncoder;
    private final RoleService roleService;
    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final EmailService emailService;

    private final PasswordResetTokenRepository passwordResetTokenRepository;

    @Value("${app.base-url}")
    private String baseUrl;

    @Value("${app.verification-path}")
    private String verificationPath;

    @Value("${app.reset-password-path}")
    private String resetPasswordPath;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserResponse register(RegisterRequest registerRequest) {
        if (userService.findByUsername(registerRequest.getUsername()) != null) {
            throw new BadRequestException("Username already exists. Please try another username");
        }

        if (userService.getByEmail(registerRequest.getEmail()) != null) {
            throw new BadRequestException("Email already exists. Please try another email");
        }

        Role userRole = roleService.getOrSave("ROLE_USER");
        User newUser = User.builder()
                .fullName(registerRequest.getFullName())
                .username(registerRequest.getUsername())
                .email(registerRequest.getEmail())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .role(userRole)
                .isVerified(false)
                .build();

        userService.save(newUser);

        String verificationUrl = baseUrl + verificationPath + "?userId=" + newUser.getId();
        String emailBody = "<h1>Verifikasi Email - Aplikasi Tekor</h1>"
                + "<p>Halo <b>" + newUser.getUsername() + "</b>,</p>"
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
    @Transactional
    public LoginResponse login(LoginRequest loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()));

            SecurityContextHolder.getContext().setAuthentication(authentication);

            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            User user = userDetails.getUser();

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
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Kredensial belum valid. Silakan periksa email Anda atau cek ulang Username/Password Anda");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void verifyEmail(UUID userId) {
        User user = userService.findById(userId.toString());

        if (Boolean.TRUE.equals(user.getIsVerified())) {
            throw new BadRequestException("Akun Anda sudah pernah diverifikasi sebelumnya.");
        }

        user.setIsVerified(true);
        userService.update(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void requestPasswordReset(ForgotPasswordRequest request) {
        User user = userService.getByEmail(request.getEmail());

        String token = UUID.randomUUID().toString();

        PasswordResetToken resetToken = PasswordResetToken.builder()
                .token(token)
                .user(user)
                .expiryDate(LocalDateTime.now().plusMinutes(15)) // Token valid selama 15 menit
                .build();
        passwordResetTokenRepository.save(resetToken);

        String resetUrl = baseUrl + resetPasswordPath + "?token=" + token;
        String emailBody = "<h1>Reset Password</h1>"
                + "<p>Anda meminta untuk mereset password Anda. Klik link di bawah untuk melanjutkan:</p>"
                + "<a href=\"" + resetUrl + "\" style=\"background-color:#DC3545;color:white;padding:15px 25px;text-align:center;text-decoration:none;display:inline-block;border-radius:8px;\">Reset Password Saya</a>"
                + "<p>Link ini hanya valid selama 15 menit. Jika Anda tidak merasa meminta ini, abaikan email ini.</p>";

        emailService.sendEmail(user.getEmail(), "Permintaan Reset Password", emailBody);
    }

    @Override
    @Transactional(readOnly = true)
    public void validatePasswordResetToken(String token) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(token)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Link reset password tidak valid atau sudah digunakan."));

        if (resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            passwordResetTokenRepository.delete(resetToken);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Link reset password sudah kedaluwarsa. Silakan ajukan permintaan baru.");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resetPassword(ResetPasswordRequest request) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("Password baru dan konfirmasi password tidak cocok.");
        }

        PasswordResetToken token = passwordResetTokenRepository.findByToken(request.getToken())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Token reset tidak valid atau sudah digunakan."));

        if (token.getExpiryDate().isBefore(LocalDateTime.now())) {
            passwordResetTokenRepository.delete(token);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Token reset sudah kedaluwarsa.");
        }

        User user = token.getUser();
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userService.update(user);

        passwordResetTokenRepository.delete(token);
    }
}
