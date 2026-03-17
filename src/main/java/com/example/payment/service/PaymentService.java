package com.example.payment.service;


import com.example.payment.decorator.PaymentLogging;
import com.example.payment.dto.PaymentRequestDTO;
import com.example.payment.dto.PaymentResponseDTO;
import com.example.payment.entity.OutboxEvent;
import com.example.payment.entity.Payment;
import com.example.payment.entity.User;
import com.example.payment.event.PaymentCreatedEvent;
import com.example.payment.factory.PaymentFactory;
import com.example.payment.model.PaymentRequest;
import com.example.payment.model.PaymentStatus;
import com.example.payment.model.PaymentType;
import com.example.payment.proxy.PaymentProxy;
import com.example.payment.repository.OutboxEventRepository;
import com.example.payment.repository.PaymentRepository;
import com.example.payment.repository.UserRepository;
import com.example.payment.strategy.PaymentStrategy;
import com.example.payment.template.DefaultPaymentProcessor;
import com.example.payment.template.PaymentProcessor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.slf4j.MDC;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class PaymentService {


    private final PaymentRepository paymentRepository;

    private final UserRepository userRepository;

    private final PaymentFactory paymentFactory;

    private final PaymentExecutionService paymentExecutionService;

    private final OutboxEventRepository outboxEventRepository;

    private final ObjectMapper objectMapper;

    public PaymentService(PaymentRepository paymentRepository,
                          UserRepository userRepository,
                          PaymentFactory paymentFactory, PaymentExecutionService paymentExecutionService, OutboxEventRepository outboxEventRepository, ObjectMapper objectMapper) {
        this.paymentRepository = paymentRepository;
        this.userRepository = userRepository;
        this.paymentFactory = paymentFactory;
        this.paymentExecutionService = paymentExecutionService;
        this.outboxEventRepository = outboxEventRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public PaymentResponseDTO processPayment(String userName ,String idempotencyKey, PaymentRequestDTO requestDTO) throws JsonProcessingException {

        // 1) If idempotency key exists check previous payment
        if (idempotencyKey != null && !idempotencyKey.isBlank()){
            Optional<Payment> existingPayment =
                    paymentRepository.findByIdempotencyKey(idempotencyKey);
            if (existingPayment.isPresent()) {
                Payment payment = existingPayment.get();
                return PaymentResponseDTO.builder()
                        .status(payment.getStatus())
                        .message("Duplicate request - returning existing payment")
                        .amount(payment.getAmount())
                        .currency(payment.getCurrency())
                        .paymentType(payment.getPaymentType())
                        .transactionId(payment.getTransactionId())
                        .build();
            }
        }

        // 2) Validate payment type
        PaymentType type = requestDTO.getPaymentType();
        if (type == null) {
            throw new IllegalArgumentException("INVALID PAYMENT TYPE");
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
        PaymentStrategy strategy = paymentFactory.getStrategy(request.getPaymentType());
        // 4) Decorators / proxy
        strategy = new PaymentProxy(strategy);
        strategy = new PaymentLogging(strategy);

        // 5) Template processor
        PaymentProcessor processor = new DefaultPaymentProcessor(strategy);

        // 6) Execute (circuit breaker via annotation)
        paymentExecutionService.execute(processor, request);

        //persist in DB
        String transactionId = "TXN-" + UUID.randomUUID();
        User user = userRepository.findByUsername(userName)
                .orElseThrow(() -> new BadCredentialsException("Invalid username or password"));

        Payment payment = new Payment();
        payment.setUser(user);
        payment.setTransactionId(transactionId); // generate unique txn id
        payment.setAmount(request.getAmount());
        payment.setCurrency(request.getCurrency());
        payment.setPaymentType(request.getPaymentType());
        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setCreatedAt(LocalDateTime.now());
        payment.setIdempotencyKey(idempotencyKey);

        Payment savedPayment = paymentRepository.save(payment);

        // 7) Publish Kafka event
        PaymentCreatedEvent event = PaymentCreatedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .transactionId(savedPayment.getTransactionId())
                .userName(userName)
                .amount(savedPayment.getAmount())
                .currency(savedPayment.getCurrency())
                .paymentType(savedPayment.getPaymentType().name())
                .status(savedPayment.getStatus().name())
                .traceId(MDC.get("traceId"))
                .build();

        // 8 Outbox pattern
        OutboxEvent outboxEvent = OutboxEvent.builder()
                .eventId(event.getEventId())
                .aggregateType("PAYMENT")
                .aggregateId(savedPayment.getTransactionId())
                .topic("payment.created")
                .payload(objectMapper.writeValueAsString(event))
                .status("NEW")
                .build();

        outboxEventRepository.save(outboxEvent);

        // 9) API response
        return PaymentResponseDTO.builder()
                .status(PaymentStatus.SUCCESS)
                .message("Payment processed successfully")
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .paymentType(request.getPaymentType())
                .transactionId(transactionId)
                .build();
    }
}
