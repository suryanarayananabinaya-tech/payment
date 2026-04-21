package com.example.payment.dto;

import com.example.payment.entity.Order;

public record OrderDTO(Long id, String status) {

    public static OrderDTO from(Order order) {
        return new OrderDTO(order.getId(), order.getStatus());
    }
}
