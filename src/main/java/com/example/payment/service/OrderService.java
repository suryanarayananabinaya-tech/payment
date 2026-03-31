package com.example.payment.service;

import com.example.payment.Util.CursorPage;
import com.example.payment.dto.OrderDTO;
import com.example.payment.entity.Order;
import com.example.payment.repository.OrderRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orderRepository;

    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }


    public CursorPage<OrderDTO> getOrders(int limit, String cursor) {
        Long cursorId = cursor == null || cursor.isEmpty() ? null : Long.parseLong(cursor);

        //fetch one page
        List<Order> orders = orderRepository.find( cursorId, PageRequest.of(0,limit));

        //nextCursor
        String nextCursor = orders.isEmpty() ? null : String.valueOf(orders.get(orders.size()-1).getId());

        return new CursorPage<>(orders.stream().map(OrderDTO::from).toList(), nextCursor);

    }
}
