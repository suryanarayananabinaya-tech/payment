package com.example.payment.controller;


import com.example.payment.dto.PaymentRequestDTO;
import com.example.payment.dto.PaymentResponseDTO;
import com.example.payment.service.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/payments")
public class PaymentController {

     private final PaymentService paymentService;

     public PaymentController( PaymentService paymentService) {

         this.paymentService = paymentService;
     }


     @PostMapping
     @PreAuthorize("hasRole('USER')")
     public PaymentResponseDTO createPayment(@RequestBody PaymentRequestDTO requestDTO) {
         Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
         if (authentication == null || !authentication.isAuthenticated()
                 || "anonymousUser".equals(authentication.getPrincipal())) {
             throw new org.springframework.security.access.AccessDeniedException("Unauthorized");
         }
       String username = authentication.getName();
       return paymentService.processPayment(username, requestDTO);

     }

}
