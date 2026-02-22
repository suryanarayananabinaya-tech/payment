package com.example.payment.factory;

import com.example.payment.model.PaymentType;
import com.example.payment.strategy.CreditCardPayment;
import com.example.payment.strategy.NetBankingPayment;
import com.example.payment.strategy.PaymentStrategy;
import com.example.payment.strategy.TypedPaymentStrategy;
import org.springframework.stereotype.Service;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PaymentFactory {

    private final Map<PaymentType, TypedPaymentStrategy> strategies;

    public PaymentFactory(List<TypedPaymentStrategy> strategyList) {
        Map<PaymentType, TypedPaymentStrategy> map = new EnumMap<>(PaymentType.class);
        for (TypedPaymentStrategy s : strategyList) {
            map.put(s.getType(), s);
        }
        this.strategies = map;
    }

    public TypedPaymentStrategy getStrategy(PaymentType type) {
        TypedPaymentStrategy strategy = strategies.get(type);
        if (strategy == null) {
            throw new IllegalArgumentException("Unsupported payment type: " + type);
        }
        return strategy;
    }
}
