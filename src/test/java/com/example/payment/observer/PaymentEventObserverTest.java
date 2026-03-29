package com.example.payment.observer;

import com.example.payment.model.PaymentRequest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PaymentEventTest {

    @Test
    void constructor_shouldSetPaymentRequest() {
        PaymentRequest request = new PaymentRequest();

        PaymentEvent event = new PaymentEvent(request);

        assertNotNull(event);
        assertEquals(request, event.getPaymentRequest());
    }
}