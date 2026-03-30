package com.example.payment.template;

import com.example.payment.model.PaymentRequest;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PaymentProcessorTest {

    @Test
    void processPayment_callsStepsInOrder() {
        List<String> steps = new ArrayList<>();
        PaymentRequest request = new PaymentRequest();

        PaymentProcessor processor = new PaymentProcessor() {
            @Override
            protected void validate(PaymentRequest request) {
                steps.add("validate");
            }

            @Override
            protected void executePayment(PaymentRequest request) {
                steps.add("execute");
            }

            @Override
            protected void postProcess(PaymentRequest request) {
                steps.add("postProcess");
            }
        };

        processor.processPayment(request);

        assertEquals(List.of("validate", "execute", "postProcess"), steps);
    }
}