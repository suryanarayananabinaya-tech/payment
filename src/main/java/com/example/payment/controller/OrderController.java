package com.example.payment.controller;

import com.example.payment.Util.CursorPage;
import com.example.payment.dto.OrderDTO;
import com.example.payment.service.OrderService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OrderController {


    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping("/orders")
    public CursorPage<OrderDTO> getListOfOrders(
            @RequestParam(defaultValue = "5") int limit,
            @RequestParam(required = false) String cursor)
    {
         return orderService.getOrders(limit,cursor);
    }
}
