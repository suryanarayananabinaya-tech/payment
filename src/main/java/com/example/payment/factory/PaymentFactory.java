package com.example.payment.factory;

import com.example.payment.model.PaymentType;
import com.example.payment.strategy.CreditCardPayment;
import com.example.payment.strategy.NetBankingPayment;
import com.example.payment.strategy.PaymentStrategy;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class PaymentFactory {

    private final Map<PaymentType, PaymentStrategy> strategies;

    public PaymentFactory(List<PaymentStrategy> strategyList) {
        Map<PaymentType, PaymentStrategy> map = new EnumMap<>(PaymentType.class);

        strategyList.forEach(strategy -> {
            if (strategy instanceof CreditCardPayment) {
                map.put(PaymentType.CARD, strategy);
            } else if (strategy instanceof NetBankingPayment) {
                map.put(PaymentType.NET_BANKING, strategy);
            }
        });

        this.strategies = map;
    }

    public PaymentStrategy getStrategy(PaymentType type) {
        PaymentStrategy strategy = strategies.get(type);
        if (strategy == null) {
            throw new IllegalArgumentException("Unsupported payment type: " + type);
        }
        return strategy;
    }
}
