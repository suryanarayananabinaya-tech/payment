package com.example.payment.controller;

import com.example.payment.Util.JWTUtil;
import com.example.payment.config.CustomUserDetailsService;
import com.example.payment.dto.PaymentRequestDTO;
import com.example.payment.dto.PaymentResponseDTO;
import com.example.payment.model.PaymentStatus;
import com.example.payment.model.PaymentType;
import com.example.payment.service.PaymentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = PaymentController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = {
                        com.example.payment.config.PaymentRateLimitFilter.class
                }
        )
)
@AutoConfigureMockMvc(addFilters = true)
@ActiveProfiles("test")
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PaymentService paymentService;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @MockBean
    private JWTUtil jwtUtil;
    // add these only if your security config requires them

    @MockBean
    private AuthenticationManager authenticationManager;

    private PaymentRequestDTO buildRequest() {
        PaymentRequestDTO requestDTO = new PaymentRequestDTO();
        requestDTO.setAmount(BigDecimal.valueOf(100.0));
        requestDTO.setCurrency("USD");
        requestDTO.setEmail("test@example.com");
        requestDTO.setPaymentType(PaymentType.CARD);
        return requestDTO;
    }

    @Test
    @WithMockUser(username = "abinaya", roles = {"USER"})
    void createPayment_success() throws Exception {
        PaymentResponseDTO responseDTO = PaymentResponseDTO.builder()
                .status(PaymentStatus.SUCCESS)
                .message("Payment processed successfully")
                .amount(BigDecimal.valueOf(100.0))
                .currency("USD")
                .paymentType(PaymentType.CARD)
                .transactionId("TXN-12345")
                .build();

        when(paymentService.processPayment(
                anyString(),
                anyString(),
                any(PaymentRequestDTO.class)
        )).thenReturn(responseDTO);

        mockMvc.perform(post("/api/v1/payments")
                        .with(csrf())
                        .header("Idempotency-Key", "12345")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("Payment processed successfully"))
                .andExpect(jsonPath("$.transactionId").value("TXN-12345"))
                .andExpect(jsonPath("$.amount").value(100.0))
                .andExpect(jsonPath("$.currency").value("USD"))
                .andExpect(jsonPath("$.paymentType").value("CARD"));

        ArgumentCaptor<PaymentRequestDTO> captor = ArgumentCaptor.forClass(PaymentRequestDTO.class);
        verify(paymentService).processPayment(eq("abinaya"), eq("12345"), captor.capture());

        assertEquals(BigDecimal.valueOf(100.0), captor.getValue().getAmount());
        assertEquals("USD", captor.getValue().getCurrency());
        assertEquals("test@example.com", captor.getValue().getEmail());
        assertEquals(PaymentType.CARD, captor.getValue().getPaymentType());
    }

    @Test
    @WithMockUser(username = "abinaya", roles = {"USER"})
    void createPayment_duplicate() throws Exception {
        PaymentResponseDTO responseDTO = PaymentResponseDTO.builder()
                .status(PaymentStatus.SUCCESS)
                .message("Duplicate request - returning existing payment")
                .amount(BigDecimal.valueOf(100.0))
                .currency("USD")
                .paymentType(PaymentType.CARD)
                .transactionId("TXN-EXISTING")
                .build();

        when(paymentService.processPayment(
                eq("abinaya"),
                eq("IDEMP-1"),
                any(PaymentRequestDTO.class)
        )).thenReturn(responseDTO);

        mockMvc.perform(post("/api/v1/payments")
                        .with(csrf())
                        .header("Idempotency-Key", "IDEMP-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Duplicate request - returning existing payment"))
                .andExpect(jsonPath("$.transactionId").value("TXN-EXISTING"));

        verify(paymentService).processPayment(eq("abinaya"), eq("IDEMP-1"), any(PaymentRequestDTO.class));
    }

    @Test
    @WithMockUser(username = "abinaya", roles = {"USER"})
    void createPayment_withoutIdempotencyKey() throws Exception {
        PaymentResponseDTO responseDTO = PaymentResponseDTO.builder()
                .status(PaymentStatus.SUCCESS)
                .message("Payment processed successfully")
                .amount(BigDecimal.valueOf(100.0))
                .currency("USD")
                .paymentType(PaymentType.CARD)
                .transactionId("TXN-999")
                .build();

        when(paymentService.processPayment(
                eq("abinaya"),
                isNull(),
                any(PaymentRequestDTO.class)
        )).thenReturn(responseDTO);

        mockMvc.perform(post("/api/v1/payments")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.transactionId").value("TXN-999"));

        verify(paymentService).processPayment(eq("abinaya"), isNull(), any(PaymentRequestDTO.class));
    }

   /* @Test
    void createPayment_unauthorized() throws Exception {
        mockMvc.perform(post("/api/v1/payments")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildRequest())))
                .andExpect(status().isUnauthorized());

        verify(paymentService, never()).processPayment(anyString(), any(), any());
    }

    @Test
    void createPayment_forbiddenWithoutCsrf() throws Exception {
        mockMvc.perform(post("/api/v1/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildRequest())))
                .andExpect(status().isForbidden());

        verify(paymentService, never()).processPayment(anyString(), any(), any());
    }*/
}