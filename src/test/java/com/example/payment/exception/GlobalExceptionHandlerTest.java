package com.example.payment.exception;

import com.example.payment.Util.JWTUtil;
import com.example.payment.config.CustomUserDetailsService;
import com.example.payment.config.PaymentRateLimitFilter;
import com.example.payment.controller.PaymentController;
import com.example.payment.service.PaymentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = PaymentController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = PaymentRateLimitFilter.class
        )
)
@Import(GlobalExceptionHandler.class)
@AutoConfigureMockMvc(addFilters = false)
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PaymentService paymentService;

    @MockBean
    private JWTUtil jwtUtil;

    @MockBean
    private UserDetailsService userDetailsService;

    @MockBean
    private AuthenticationManager authenticationManager;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    @WithMockUser(username = "abinaya", roles = {"USER"})
    void testIllegalArgumentException() throws Exception {
        when(paymentService.processPayment(anyString(), any(), any()))
                .thenThrow(new IllegalArgumentException("INVALID PAYMENT TYPE"));

        mockMvc.perform(post("/api/v1/payments")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "amount": 100,
                                  "currency": "USD",
                                  "email": "test@example.com",
                                  "paymentType": "CARD"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").value("INVALID PAYMENT TYPE"));
    }

    @Test
    @WithMockUser(username = "abinaya", roles = {"USER"})
    void testExternalServiceException() throws Exception {
        when(paymentService.processPayment(anyString(), any(), any()))
                .thenThrow(new ExternalServiceException("gateway failure"));

        mockMvc.perform(post("/api/v1/payments")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "amount": 100,
                                  "currency": "USD",
                                  "email": "test@example.com",
                                  "paymentType": "CARD"
                                }
                                """))
                .andExpect(status().isServiceUnavailable());
    }

    @Test
    @WithMockUser(username = "abinaya", roles = {"USER"})
    void testGenericException() throws Exception {
        when(paymentService.processPayment(anyString(), any(), any()))
                .thenThrow(new RuntimeException("unexpected failure"));

        mockMvc.perform(post("/api/v1/payments")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "amount": 100,
                                  "currency": "USD",
                                  "email": "test@example.com",
                                  "paymentType": "CARD"
                                }
                                """))
                .andExpect(status().isInternalServerError());
    }
}