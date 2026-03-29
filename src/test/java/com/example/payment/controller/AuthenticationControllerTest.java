package com.example.payment.controller;

import com.example.payment.dto.AuthResponseDTO;
import com.example.payment.dto.LoginRequestDTO;
import com.example.payment.dto.RefreshTokenRequestDTO;
import com.example.payment.dto.RegisterRequest;
import com.example.payment.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = AuthenticationController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = {
                        com.example.payment.config.JWTAuthenticationFilter.class,
                        com.example.payment.config.PaymentRateLimitFilter.class
                }
        )
)
@AutoConfigureMockMvc(addFilters = false)
class AuthenticationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @Test
    void register_success() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("abinaya");
        request.setEmail("abinaya@test.com");
        request.setPassword("Password@123");

        doNothing().when(authService).register(any(RegisterRequest.class));

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("User created successfully"));

        verify(authService).register(any(RegisterRequest.class));
    }

    @Test
    void login_success() throws Exception {
        LoginRequestDTO request = new LoginRequestDTO();
        request.setUsername("abinaya");
        request.setPassword("Password@123");

        AuthResponseDTO response = new AuthResponseDTO(
                "access-token-123",
                "refresh-token-123"
        );

        when(authService.login("abinaya", "Password@123"))
                .thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access-token-123"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-token-123"));

        verify(authService).login("abinaya", "Password@123");
    }

    @Test
    void refresh_success() throws Exception {
        RefreshTokenRequestDTO request = new RefreshTokenRequestDTO();
        request.setRefreshToken("refresh-token-123");

        AuthResponseDTO response = new AuthResponseDTO(
                "new-access-token",
                "new-refresh-token"
        );

        when(authService.refreshToken("refresh-token-123"))
                .thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("new-access-token"))
                .andExpect(jsonPath("$.refreshToken").value("new-refresh-token"));

        verify(authService).refreshToken("refresh-token-123");
    }
}