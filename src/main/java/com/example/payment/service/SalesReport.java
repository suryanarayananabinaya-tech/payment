package com.example.payment.service;

import com.example.payment.strategy.Report;
import com.example.payment.strategy.ReportVisitor;
import lombok.Getter;

@Getter
public class SalesReport implements Report {

    private final String data = " My sales report data";


    @Override
    public void accept(ReportVisitor visitor) {
           visitor.visit(this);
    }
}
