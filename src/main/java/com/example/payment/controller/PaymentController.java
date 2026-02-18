package com.example.payment.controller;


import com.example.payment.dto.PaymentRequestDTO;
import com.example.payment.dto.PaymentResponseDTO;
import com.example.payment.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/payments")
public class PaymentController {


     @Autowired
     PaymentService paymentService;

     PaymentController( PaymentService paymentService) {
         this.paymentService = paymentService;
     }


     @PostMapping
     public PaymentResponseDTO createPayment(@RequestBody PaymentRequestDTO request){

        return paymentService.processPayment(request);
     }

}
