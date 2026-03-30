package com.example.payment.controller;


import com.example.payment.dto.PaymentRequestDTO;
import com.example.payment.dto.PaymentResponseDTO;
import com.example.payment.service.PaymentService;
import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payments")
public class PaymentController {

     private final PaymentService paymentService;

     public PaymentController( PaymentService paymentService) {

         this.paymentService = paymentService;
     }


     @PostMapping
     @PreAuthorize("hasRole('USER')")
     public ResponseEntity<PaymentResponseDTO> createPayment(
             @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
             @Valid @RequestBody PaymentRequestDTO requestDTO,
             @AuthenticationPrincipal UserDetails userDetails) throws JsonProcessingException {
         PaymentResponseDTO response =
                 paymentService.processPayment(userDetails.getUsername(), idempotencyKey, requestDTO);

         return ResponseEntity.status(HttpStatus.CREATED).body(response);

     }

}
