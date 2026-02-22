package com.example.payment.service;

import com.example.payment.Util.JWTUtil;
import com.example.payment.dto.RegisterRequest;
import com.example.payment.entity.User;
import com.example.payment.repository.UserRepository;
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

    public Map<String, String> login(String username, String password) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Invalid username"));
        if(!passwordEncoder.matches(password, user.getPassword())){
            throw new RuntimeException("Invalid Password");
        }

        String token = jwtUtil.generateToken(user.getUsername());
        String refreshToken = jwtUtil.generateToken(user.getUsername());
        Map<String, String> map = new HashMap<>();
        map.put("token", token);
        map.put("refreshToken", refreshToken);
        return map;
    }

    public Map<String,String> refreshToken(String refreshToken){
        String username = jwtUtil.extractUsername(refreshToken);
        String token = jwtUtil.generateToken(username);
        Map<String,String> map = new HashMap<>();
        return Map.of("token",token);
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
