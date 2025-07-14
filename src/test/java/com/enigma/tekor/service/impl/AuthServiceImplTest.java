package com.enigma.tekor.service.impl;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.enigma.tekor.dto.request.LoginRequest;
import com.enigma.tekor.dto.request.RegisterRequest;
import com.enigma.tekor.dto.response.LoginResponse;
import com.enigma.tekor.entity.EmailVerificationToken;
import com.enigma.tekor.entity.Role;
import com.enigma.tekor.entity.User;
import com.enigma.tekor.exception.BadRequestException;
import com.enigma.tekor.repository.EmailVerificationTokenRepository;
import com.enigma.tekor.repository.PasswordResetTokenRepository;
import com.enigma.tekor.security.CustomUserDetails;
import com.enigma.tekor.service.EmailService;
import com.enigma.tekor.service.RoleService;
import com.enigma.tekor.service.UserService;
import com.enigma.tekor.util.JwtUtil;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private RoleService roleService;

    @Mock
    private UserService userService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private EmailService emailService;

    @Mock
    private EmailVerificationTokenRepository emailVerificationTokenRepository;

    @Mock
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @InjectMocks
    private AuthServiceImpl authService;

    private RegisterRequest registerRequest;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest();
        registerRequest.setFullName("Test User");
        registerRequest.setUsername("testuser");
        registerRequest.setEmail("test@example.com");
        registerRequest.setPassword("password");
    }

    @Test
    void testRegister_Success() {
        // Given
        when(userService.findByUsername(anyString())).thenReturn(null);
        when(userService.getByEmail(anyString())).thenReturn(null);
        when(roleService.getOrSave(anyString())).thenReturn(new Role());
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");

        // When
        var userResponse = authService.register(registerRequest);

        // Then
        assertNotNull(userResponse);
        assertEquals(registerRequest.getUsername(), userResponse.getUsername());
        verify(userService, times(1)).save(any(User.class));
        verify(emailService, times(1)).sendEmail(anyString(), anyString(), anyString());
    }

    @Test
    void testRegister_UsernameExists() {
        // Given
        when(userService.findByUsername(anyString())).thenReturn(new User());

        // When & Then
        assertThrows(BadRequestException.class, () -> {
            authService.register(registerRequest);
        });
        verify(userService, never()).save(any(User.class));
    }

    @Test
    void testRegister_EmailExists() {
        // Given
        when(userService.findByUsername(anyString())).thenReturn(null);
        when(userService.getByEmail(anyString())).thenReturn(new User());

        // When & Then
        assertThrows(BadRequestException.class, () -> {
            authService.register(registerRequest);
        });
        verify(userService, never()).save(any(User.class));
    }

    @Test
    void testLogin_Success() {
        // Given
        LoginRequest loginRequest = new LoginRequest("testuser", "password");
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setRole(new Role(null, "ROLE_USER"));
        CustomUserDetails userDetails = new CustomUserDetails(user);
        Authentication authentication = mock(Authentication.class);

        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(jwtUtil.generateAccessToken(any(User.class))).thenReturn("accessToken");
        when(jwtUtil.generateRefreshToken(any(User.class))).thenReturn("refreshToken");

        // When
        LoginResponse response = authService.login(loginRequest);

        // Then
        assertNotNull(response);
        assertEquals("accessToken", response.getToken().getAccessToken());
        verify(authenticationManager).authenticate(any());
    }

    @Test
    void testVerifyEmail_Success() {
        // Given
        User user = new User();
        user.setIsVerified(false);
        EmailVerificationToken token = new EmailVerificationToken(user, "token");
        token.setExpiryDate(new Date(System.currentTimeMillis() + 100000));
        when(emailVerificationTokenRepository.findByToken("token")).thenReturn(Optional.of(token));

        // When
        authService.verifyEmail("token");

        // Then
        assertTrue(user.getIsVerified());
        verify(emailVerificationTokenRepository).delete(token);
    }
}
