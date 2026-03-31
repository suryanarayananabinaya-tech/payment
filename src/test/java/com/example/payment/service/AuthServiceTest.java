package com.example.payment.service;

import com.example.payment.Util.JWTUtil;
import com.example.payment.dto.AuthResponseDTO;
import com.example.payment.dto.RegisterRequest;
import com.example.payment.entity.User;
import com.example.payment.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private JWTUtil jwtUtil;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    @Test
    void login_success() {
        User user = new User();
        user.setUsername("abinaya");
        user.setPassword("encoded-password");

        when(userRepository.findByUsername("abinaya"))
                .thenReturn(Optional.of(user));
        when(passwordEncoder.matches("Password@123", "encoded-password"))
                .thenReturn(true);
        when(jwtUtil.generateAccessToken("abinaya"))
                .thenReturn("access-token");
        when(jwtUtil.generateRefreshToken("abinaya"))
                .thenReturn("refresh-token");

        AuthResponseDTO response = authService.login("abinaya", "Password@123");

        assertEquals("access-token", response.getAccessToken());
        assertEquals("refresh-token", response.getRefreshToken());

        verify(userRepository).findByUsername("abinaya");
        verify(passwordEncoder).matches("Password@123", "encoded-password");
        verify(jwtUtil).generateAccessToken("abinaya");
        verify(jwtUtil).generateRefreshToken("abinaya");
    }

    @Test
    void login_userNotFound() {
        when(userRepository.findByUsername("abinaya"))
                .thenReturn(Optional.empty());

        BadCredentialsException ex = assertThrows(
                BadCredentialsException.class,
                () -> authService.login("abinaya", "Password@123")
        );

        assertEquals("Invalid username or password", ex.getMessage());

        verify(userRepository).findByUsername("abinaya");
        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(jwtUtil, never()).generateAccessToken(anyString());
        verify(jwtUtil, never()).generateRefreshToken(anyString());
    }

    @Test
    void login_invalidPassword() {
        User user = new User();
        user.setUsername("abinaya");
        user.setPassword("encoded-password");

        when(userRepository.findByUsername("abinaya"))
                .thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong-password", "encoded-password"))
                .thenReturn(false);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> authService.login("abinaya", "wrong-password")
        );

        assertEquals("Invalid username or Password", ex.getMessage());

        verify(userRepository).findByUsername("abinaya");
        verify(passwordEncoder).matches("wrong-password", "encoded-password");
        verify(jwtUtil, never()).generateAccessToken(anyString());
        verify(jwtUtil, never()).generateRefreshToken(anyString());
    }

    @Test
    void refreshToken_success() {
        User user = new User();
        user.setUsername("abinaya");

        when(jwtUtil.isRefreshTokenValid("valid-refresh-token"))
                .thenReturn(true);
        when(jwtUtil.extractUsername("valid-refresh-token"))
                .thenReturn("abinaya");
        when(userRepository.findByUsername("abinaya"))
                .thenReturn(Optional.of(user));
        when(jwtUtil.generateAccessToken("abinaya"))
                .thenReturn("new-access-token");
        when(jwtUtil.generateRefreshToken("abinaya"))
                .thenReturn("new-refresh-token");

        AuthResponseDTO response = authService.refreshToken("valid-refresh-token");

        assertEquals("new-access-token", response.getAccessToken());
        assertEquals("new-refresh-token", response.getRefreshToken());

        verify(jwtUtil).isRefreshTokenValid("valid-refresh-token");
        verify(jwtUtil).extractUsername("valid-refresh-token");
        verify(userRepository).findByUsername("abinaya");
        verify(jwtUtil).generateAccessToken("abinaya");
        verify(jwtUtil).generateRefreshToken("abinaya");
    }

    @Test
    void refreshToken_invalidToken() {
        when(jwtUtil.isRefreshTokenValid("bad-token"))
                .thenReturn(false);

        BadCredentialsException ex = assertThrows(
                BadCredentialsException.class,
                () -> authService.refreshToken("bad-token")
        );

        assertEquals("Invalid refresh token", ex.getMessage());

        verify(jwtUtil).isRefreshTokenValid("bad-token");
        verify(jwtUtil, never()).extractUsername(anyString());
        verify(userRepository, never()).findByUsername(anyString());
        verify(jwtUtil, never()).generateAccessToken(anyString());
        verify(jwtUtil, never()).generateRefreshToken(anyString());
    }

    @Test
    void refreshToken_userNotFound() {
        when(jwtUtil.isRefreshTokenValid("valid-refresh-token"))
                .thenReturn(true);
        when(jwtUtil.extractUsername("valid-refresh-token"))
                .thenReturn("abinaya");
        when(userRepository.findByUsername("abinaya"))
                .thenReturn(Optional.empty());

        BadCredentialsException ex = assertThrows(
                BadCredentialsException.class,
                () -> authService.refreshToken("valid-refresh-token")
        );

        assertEquals("Invalid refresh token", ex.getMessage());

        verify(jwtUtil).isRefreshTokenValid("valid-refresh-token");
        verify(jwtUtil).extractUsername("valid-refresh-token");
        verify(userRepository).findByUsername("abinaya");
        verify(jwtUtil, never()).generateAccessToken(anyString());
        verify(jwtUtil, never()).generateRefreshToken(anyString());
    }

    @Test
    void register_success() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("abinaya");
        request.setEmail("abinaya@test.com");
        request.setPassword("Password@123");

        when(userRepository.findByUsername("abinaya"))
                .thenReturn(Optional.empty());
        when(passwordEncoder.encode("Password@123"))
                .thenReturn("encoded-password");

        authService.register(request);

        verify(userRepository).findByUsername("abinaya");
        verify(passwordEncoder).encode("Password@123");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_usernameExists() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("abinaya");
        request.setEmail("abinaya@test.com");
        request.setPassword("Password@123");

        User existingUser = new User();
        existingUser.setUsername("abinaya");

        when(userRepository.findByUsername("abinaya"))
                .thenReturn(Optional.of(existingUser));

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> authService.register(request)
        );

        assertEquals("Username already exists", ex.getMessage());

        verify(userRepository).findByUsername("abinaya");
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }
}