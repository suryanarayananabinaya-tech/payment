package com.example.payment.factory;

import com.example.payment.model.PaymentType;
import com.example.payment.strategy.TypedPaymentStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class PaymentFactoryTest {

    private TypedPaymentStrategy cardStrategy;
    private TypedPaymentStrategy netBankingStrategy;
    private PaymentFactory paymentFactory;

    @BeforeEach
    void setUp() {
        cardStrategy = Mockito.mock(TypedPaymentStrategy.class);
        netBankingStrategy = Mockito.mock(TypedPaymentStrategy.class);

        when(cardStrategy.getType()).thenReturn(PaymentType.CARD);
        when(netBankingStrategy.getType()).thenReturn(PaymentType.NET_BANKING);

        paymentFactory = new PaymentFactory(List.of(cardStrategy, netBankingStrategy));
    }

    @Test
    void getStrategy_shouldReturnCardStrategy_whenTypeIsCard() {
        TypedPaymentStrategy result = paymentFactory.getStrategy(PaymentType.CARD);

        assertNotNull(result);
        assertEquals(cardStrategy, result);
    }

    @Test
    void getStrategy_shouldReturnNetBankingStrategy_whenTypeIsNetBanking() {
        TypedPaymentStrategy result = paymentFactory.getStrategy(PaymentType.NET_BANKING);

        assertNotNull(result);
        assertEquals(netBankingStrategy, result);
    }

    @Test
    void getStrategy_shouldThrowException_whenTypeIsMissingInFactory() {

        // Only CARD strategy registered
        when(cardStrategy.getType()).thenReturn(PaymentType.CARD);

        PaymentFactory factory = new PaymentFactory(List.of(cardStrategy));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> factory.getStrategy(PaymentType.NET_BANKING)
        );

        assertEquals("Unsupported payment type: NET_BANKING", exception.getMessage());
    }
}