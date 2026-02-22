package com.example.payment.controller;

import com.example.payment.dto.AuthResponseDTO;
import com.example.payment.dto.LoginRequestDTO;
import com.example.payment.dto.RegisterRequest;
import com.example.payment.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthenticationController {


    private final AuthService authService;

    public AuthenticationController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        return ResponseEntity.ok("User created successfully");
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody LoginRequestDTO request){
        AuthResponseDTO tokens = authService.login(
                request.getUsername(),
                request.getPassword()
        );
        return ResponseEntity.ok(tokens);
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponseDTO> refreshToken(@RequestParam String refreshToken){
        AuthResponseDTO tokens = authService.refreshToken(refreshToken);
        return ResponseEntity.ok(tokens);
    }
}
