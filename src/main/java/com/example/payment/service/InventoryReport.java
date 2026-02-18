package com.example.payment.service;

import com.example.payment.strategy.Report;
import com.example.payment.strategy.ReportVisitor;
import lombok.Getter;

@Getter
public class InventoryReport implements Report {

    private final String data = "My Inventory Report";


    @Override
    public void accept(ReportVisitor visitor) {
        visitor.visit(this);
    }
}
