package com.enigma.tekor.service.impl;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.enigma.tekor.dto.request.ChangePasswordRequest;
import com.enigma.tekor.dto.request.UpdateProfileRequest;
import com.enigma.tekor.dto.response.ProfileResponse;
import com.enigma.tekor.entity.User;
import com.enigma.tekor.exception.UserNotFoundException;
import com.enigma.tekor.repository.UserRepository;
import com.enigma.tekor.service.CloudinaryService;
import com.enigma.tekor.service.TransactionService;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private CloudinaryService cloudinaryService;

    @Mock
    private TransactionService transactionService;

    @InjectMocks
    private UserServiceImpl userService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("test@example.com");
        user.setFullName("Test User");
        user.setUsername("testuser");
        user.setPassword("password");
    }

    @Test
    void testGetProfileById_UserFound() {
        // Given
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        // When
        var profileResponse = userService.getProfileById(user.getId());

        // Then
        assertNotNull(profileResponse);
        assertEquals(user.getFullName(), profileResponse.getFullName());
        verify(userRepository, times(1)).findById(user.getId());
    }

    @Test
    void testGetProfileById_UserNotFound() {
        // Given
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(UserNotFoundException.class, () -> {
            userService.getProfileById(userId);
        });
        verify(userRepository, times(1)).findById(userId);
    }
    @Test
    void testUpdateProfile_Success() {
        // Given
        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setFullName("New Name");

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        ProfileResponse response = userService.updateProfile(user.getId(), request);

        // Then
        assertNotNull(response);
        assertEquals("New Name", response.getFullName());
        verify(userRepository).findById(user.getId());
        verify(userRepository).save(user);
    }

    @Test
    void testGetUserIdByUsername_Success() {
        // Given
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

        // When
        UUID foundId = userService.getUserIdByUsername(user.getEmail());

        // Then
        assertNotNull(foundId);
        assertEquals(user.getId(), foundId);
        verify(userRepository).findByEmail(user.getEmail());
    }

    @Test
    void testChangePassword_Success() {
        // Given
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword("password");
        request.setNewPassword("newPassword");
        request.setConfirmNewPassword("newPassword");

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password", "password")).thenReturn(true);

        // When
        userService.changePassword(user.getId(), request);

        // Then
        verify(passwordEncoder).encode("newPassword");
        verify(userRepository).save(user);
    }

    @Test
    void testFindById_Success() {
        // Given
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        // When
        User foundUser = userService.findById(user.getId());

        // Then
        assertNotNull(foundUser);
        assertEquals(user.getId(), foundUser.getId());
        verify(userRepository).findById(user.getId());
    }
}
