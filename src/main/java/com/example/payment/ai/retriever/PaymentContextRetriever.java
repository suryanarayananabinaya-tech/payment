package com.example.payment.ai.retriever;

import com.example.payment.ai.dto.PaymentContextDto;
import com.example.payment.ai.mapper.PaymentContextMapper;
import com.example.payment.entity.Payment;
import com.example.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentContextRetriever {

    private final PaymentRepository paymentRepository;
    private final PaymentContextMapper paymentContextMapper;

    public PaymentContextDto retrieve(String username) {
        log.debug("Retrieving payment context [username={}]", username);

        List<Payment> payments = paymentRepository
                .findTop10ByUserUsernameOrderByCreatedAtDesc(username);

        PaymentContextDto context = paymentContextMapper.toDto(username, payments);

        log.debug("Payment context retrieved [username={}, count={}]",
                username, payments.size());

        return context;
    }
}
