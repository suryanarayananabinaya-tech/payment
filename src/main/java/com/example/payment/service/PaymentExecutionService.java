package com.example.payment.service;

import com.example.payment.exception.ExternalServiceException;
import com.example.payment.model.PaymentRequest;
import com.example.payment.template.PaymentProcessor;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.stereotype.Service;

@Service
public class PaymentExecutionService {

    @Retry(name = "paymentServiceRetry", fallbackMethod = "fallbackPayment")
    @CircuitBreaker(name = "paymentServiceCircuit", fallbackMethod = "fallbackPayment")
    public void execute(PaymentProcessor processor, PaymentRequest request) {
        processor.processPayment(request);

    }

    public void fallbackPayment(PaymentProcessor processor,
                                PaymentRequest request,
                                Throwable t) {

        throw new ExternalServiceException(
                "External payment service unavailable. Try later."
        );
    }
}