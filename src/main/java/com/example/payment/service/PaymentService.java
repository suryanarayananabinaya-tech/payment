package com.example.payment.service;


import com.example.payment.decorator.PaymentCircuitBreaker;
import com.example.payment.decorator.PaymentLogging;
import com.example.payment.dto.PaymentRequestDTO;
import com.example.payment.dto.PaymentResponseDTO;
import com.example.payment.entity.Payment;
import com.example.payment.entity.User;
import com.example.payment.factory.PaymentFactory;
import com.example.payment.model.PaymentRequest;
import com.example.payment.model.PaymentStatus;
import com.example.payment.model.PaymentType;
import com.example.payment.observer.PaymentEvent;
import com.example.payment.proxy.PaymentProxy;
import com.example.payment.repository.PaymentRepository;
import com.example.payment.repository.UserRepository;
import com.example.payment.strategy.PaymentStrategy;
import com.example.payment.strategy.Report;
import com.example.payment.strategy.ReportVisitor;
import com.example.payment.template.DefaultPaymentProcessor;
import com.example.payment.template.PaymentProcessor;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PaymentService {


    private final ApplicationEventPublisher applicationEventPublisher;

    private final PaymentRepository paymentRepository;

    private final UserRepository userRepository;

    private final CircuitBreakerRegistry circuitBreakerRegistry;

    private final PaymentFactory paymentFactory;

    public PaymentService(ApplicationEventPublisher publisher,
                          PaymentRepository paymentRepository, UserRepository userRepository,
                          CircuitBreakerRegistry circuitBreakerRegistry, PaymentFactory paymentFactory) {
        this.applicationEventPublisher = publisher;
        this.paymentRepository = paymentRepository;
        this.userRepository = userRepository;
        this.circuitBreakerRegistry = circuitBreakerRegistry;
        this.paymentFactory = paymentFactory;
    }

    PaymentStrategy paymentStrategy;


    public PaymentResponseDTO processPayment(String userName , PaymentRequestDTO requestDTO) {

        // 1) Validate payment type (DTO already has enum, just null-check)
        PaymentType type = requestDTO.getPaymentType();
        if (type == null) {
            return PaymentResponseDTO.builder()
                    .status(PaymentStatus.FAILED)
                    .message("INVALID PAYMENT TYPE")
                    .amount(requestDTO.getAmount())
                    .paymentType(null)
                    .build();
        }

        // 2) Build immutable domain model
        PaymentRequest request = PaymentRequest.builder()
                .amount(requestDTO.getAmount())
                .currency(requestDTO.getCurrency())
                .email(requestDTO.getEmail())
                .paymentType(type)
                .userName(userName)
                .build();

        // 3) Select strategy via DI factory
        PaymentStrategy strategy;
        try {
            strategy = paymentFactory.getStrategy(request.getPaymentType());
        } catch (IllegalArgumentException e) {
            return PaymentResponseDTO.builder()
                    .status(PaymentStatus.FAILED)
                    .message("INVALID PAYMENT TYPE")
                    .amount(request.getAmount())
                    .paymentType(request.getPaymentType())
                    .build();
        }

        // 4) Decorators / proxy (optional cross-cutting)
        strategy = new PaymentProxy(strategy);
        strategy = new PaymentLogging(strategy);

        // 5) Template processor
        PaymentProcessor processor = new DefaultPaymentProcessor(strategy);

        // 6) Execute (circuit breaker via annotation)
        try {
            executeWithCircuitBreaker(processor, request);
        } catch (RuntimeException ex) {
            return PaymentResponseDTO.builder()
                    .status(PaymentStatus.FAILED)
                    .message(ex.getMessage())
                    .amount(request.getAmount())
                    .paymentType(request.getPaymentType())
                    .build();
        }
        //persist in DB
        Payment payment = new Payment();
        User user = userRepository.findByUsername(userName)
                .orElseThrow(() -> new BadCredentialsException("Invalid username or password"));

        payment.setUser(user); // <-- set User object, not userId
        payment.setTransactionId("TXN-" + System.currentTimeMillis()); // generate unique txn id
        payment.setAmount(request.getAmount());
        payment.setCurrency(request.getCurrency());
        payment.setPaymentType(request.getPaymentType());
        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setCreatedAt(LocalDateTime.now());

        paymentRepository.save(payment);

        // 8) Observer event
        applicationEventPublisher.publishEvent(new PaymentEvent(request));

        // 9) API response
        return PaymentResponseDTO.builder()
                .status(PaymentStatus.SUCCESS)
                .message("Payment processed successfully")
                .amount(request.getAmount())
                .paymentType(request.getPaymentType())
                .build();
    }

    @CircuitBreaker(name = "paymentServiceCircuit", fallbackMethod = "fallbackPayment")
    public void executeWithCircuitBreaker(PaymentProcessor processor, PaymentRequest request) {
        processor.processPayment(request);
    }

    public void fallbackPayment(PaymentProcessor processor, PaymentRequest request, Throwable t) {
        throw new RuntimeException("External payment service unavailable. Try later.");
    }

    public void generateReport() {
        List<Report> reports = List.of(new SalesReport(), new InventoryReport());
        //pdf//
        ReportVisitor pdfReportVisitor = new PDFReportVisitor();
        reports.forEach(report -> {report.accept(pdfReportVisitor);});

        //csv//
        ReportVisitor csvReportVisitor = new CSVReportVisitor();
        reports.forEach(report -> {report.accept(csvReportVisitor);});

    }
}
