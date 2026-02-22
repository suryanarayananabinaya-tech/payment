package com.example.payment.decorator;

import com.example.payment.model.PaymentRequest;
import com.example.payment.model.PaymentType;
import com.example.payment.strategy.PaymentStrategy;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PaymentCircuitBreaker implements PaymentStrategy {

    private static final Logger logger = LoggerFactory.getLogger(PaymentCircuitBreaker.class);
    
    private final PaymentStrategy paymentStrategy;
    private final CircuitBreaker circuitBreaker;

    public PaymentCircuitBreaker(PaymentStrategy paymentStrategy,
                                 CircuitBreakerRegistry registry,
                                 String circuitName) {
        this.paymentStrategy = paymentStrategy;
        this.circuitBreaker = registry.circuitBreaker(circuitName);
    }

    @Override
    public void pay(PaymentRequest paymentRequest) {
        try{
            circuitBreaker.executeRunnable(() -> paymentStrategy.pay(paymentRequest));
        }catch (Exception e){
            logger.error("Payment failed via circuit breaker", e);
            throw new IllegalArgumentException("External payment service unavailable. Try later.");
        }

    }
}
