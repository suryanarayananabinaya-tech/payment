package com.example.payment.service;

import com.example.payment.Util.JWTUtil;
import com.example.payment.dto.AuthResponseDTO;
import com.example.payment.dto.RegisterRequest;
import com.example.payment.entity.User;
import com.example.payment.repository.UserRepository;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class AuthService {

    private final JWTUtil jwtUtil;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(JWTUtil jwtUtil,
                       UserRepository userRepository,
                       PasswordEncoder passwordEncoder) {
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public AuthResponseDTO login(String username, String password) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BadCredentialsException("Invalid username or password"));

        if(!passwordEncoder.matches(password, user.getPassword())){
            throw new RuntimeException("Invalid username or Password");
        }

        String accessToken = jwtUtil.generateAccessToken(user.getUsername());
        String refreshToken = jwtUtil.generateRefreshToken(user.getUsername());

        return new  AuthResponseDTO(accessToken, refreshToken);
    }

    public AuthResponseDTO refreshToken(String refreshToken){
        // 1) Validate refresh token (signature + expiry + type)
        if (!jwtUtil.isRefreshTokenValid(refreshToken)) {
            throw new BadCredentialsException("Invalid refresh token");
        }
        // 2) Extract username
        String username = jwtUtil.extractUsername(refreshToken);

        // 3) Ensure user still exists/active
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BadCredentialsException("Invalid refresh token"));

        // 4) Issue new tokens (rotate refresh token)
        String newAccessToken = jwtUtil.generateAccessToken(user.getUsername());
        String newRefreshToken = jwtUtil.generateRefreshToken(user.getUsername());

        return new AuthResponseDTO(newAccessToken, newRefreshToken);

    }

    public void register(RegisterRequest request) {
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new RuntimeException("Username already exists");
        }
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        userRepository.save(user);
    }
}
