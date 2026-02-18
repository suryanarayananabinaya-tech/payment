package com.example.payment.controller;

import com.example.payment.Util.JWTUtil;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthenticationController {

    private final JWTUtil jwtUtil;

    public AuthenticationController(JWTUtil jwtUtil) {
        this.jwtUtil = jwtUtil;

    }

    @PostMapping("/login")
    public String login (@RequestParam String userName, @RequestParam String password){
        return jwtUtil.generateToken(userName);
    }
}
