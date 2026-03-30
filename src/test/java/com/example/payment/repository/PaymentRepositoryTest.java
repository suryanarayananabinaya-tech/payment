package com.example.payment.repository;

import com.example.payment.entity.Payment;
import com.example.payment.entity.User;
import com.example.payment.model.PaymentStatus;
import com.example.payment.model.PaymentType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
// ❌ REMOVE Replace.NONE
class PaymentRepositoryTest {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void findByIdempotencyKey_found() {

        User user = new User();
        user.setEmail("test@example.com");
        user.setUsername("testuser");
        user.setPassword("testpass");
        user = userRepository.save(user);

        Payment payment = new Payment();
        payment.setUser(user);
        payment.setTransactionId("txn-123");
        payment.setAmount(BigDecimal.valueOf(100));
        payment.setCurrency("USD");
        payment.setPaymentType(PaymentType.CARD);
        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setCreatedAt(LocalDateTime.now());
        payment.setIdempotencyKey("abc123");

        paymentRepository.save(payment);

        Optional<Payment> result =
                paymentRepository.findByIdempotencyKey("abc123");

        assertTrue(result.isPresent());
        assertEquals("abc123", result.get().getIdempotencyKey());
    }
}