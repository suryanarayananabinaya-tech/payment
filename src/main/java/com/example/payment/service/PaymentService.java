package com.example.payment.service;


import com.example.payment.decorator.PaymentCircuitBreaker;
import com.example.payment.decorator.PaymentLogging;
import com.example.payment.dto.PaymentRequestDTO;
import com.example.payment.dto.PaymentResponseDTO;
import com.example.payment.entity.Payment;
import com.example.payment.entity.User;
import com.example.payment.factory.PaymentFactory;
import com.example.payment.model.PaymentRequest;
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
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PaymentService {


    private final ApplicationEventPublisher applicationEventPublisher;

    private final PaymentRepository paymentRepository;

    private final UserRepository userRepository;

    private final CircuitBreakerRegistry circuitBreakerRegistry;

    public PaymentService(ApplicationEventPublisher publisher,
                          PaymentRepository paymentRepository, UserRepository userRepository,
                          CircuitBreakerRegistry circuitBreakerRegistry) {
        this.applicationEventPublisher = publisher;
        this.paymentRepository = paymentRepository;
        this.userRepository = userRepository;
        this.circuitBreakerRegistry = circuitBreakerRegistry;
    }

    PaymentStrategy paymentStrategy;


    public PaymentResponseDTO processPayment(String userName , PaymentRequestDTO requestDTO) {

        // =====================
        // Build Immutable Model
        // =====================
        PaymentRequest request = PaymentRequest.builder()
                .amount(requestDTO.getAmount())
                .currency(requestDTO.getCurrency())
                .email(requestDTO.getEmail())
                .paymentType(PaymentRequest.PaymentType.valueOf(requestDTO.getPaymentType().toUpperCase()))
                .userId(userName)
                .build();

        // Get Strategy
        try{
            paymentStrategy = PaymentFactory.getStrategy(String.valueOf(request.getPaymentType()));
        }catch (IllegalArgumentException e){
           return PaymentResponseDTO.builder()
                   .status("FAILED")
                   .message("INVALID PAYMENT TYPE")
                   .amount(request.getAmount())
                   .paymentType(String.valueOf(request.getPaymentType()))
                   .build();
        }

        //decorator
        paymentStrategy = new PaymentCircuitBreaker(paymentStrategy,circuitBreakerRegistry, "PaymentCircuit" );
        paymentStrategy = new PaymentProxy(paymentStrategy);
        paymentStrategy = new PaymentLogging(paymentStrategy);

        //process payment
        PaymentProcessor processor = new DefaultPaymentProcessor(paymentStrategy);
        try {
            // Method annotated for circuit breaker
            executeWithCircuitBreaker(processor, request);
        } catch (RuntimeException ex) {
            //  If validation or strategy fails, return failure response
            return PaymentResponseDTO.builder()
                    .status("FAILED")
                    .message(ex.getMessage())
                    .amount(request.getAmount())
                    .paymentType(String.valueOf(request.getPaymentType()))
                    .build();
        }
        //persist in DB
        Payment payment = new Payment();
        User user = userRepository.findByUsername(userName)
                .orElseThrow(() -> new RuntimeException("Invalid username or password"));

        payment.setUser(user); // <-- set User object, not userId
        payment.setTransactionId("TXN-" + System.currentTimeMillis()); // generate unique txn id
        payment.setAmount(request.getAmount());
        payment.setCurrency(request.getCurrency());
        payment.setPaymentType(com.example.payment.entity.Payment.PaymentType.valueOf(requestDTO.getPaymentType()));
        payment.setStatus("SUCCESS");
        payment.setCreatedAt(LocalDateTime.now());

        paymentRepository.save(payment);

        // observer for Notifications
        applicationEventPublisher.publishEvent(new PaymentEvent(request));

        // 7️⃣ Return API Response
        return PaymentResponseDTO.builder()
                .status("SUCCESS")
                .message("Payment processed successfully")
                .amount(request.getAmount())
                .paymentType(String.valueOf(request.getPaymentType()))
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
