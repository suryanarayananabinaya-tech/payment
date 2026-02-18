package com.example.payment.service;

import com.example.payment.strategy.ReportVisitor;

public class CSVReportVisitor implements ReportVisitor {
    @Override
    public void visit(SalesReport salesReport) {
        System.out.println("Generating CSV for"+ salesReport.getData());

    }

    @Override
    public void visit(InventoryReport inventoryReport) {
        System.out.println("Generating PDF for"+ inventoryReport.getData());

    }
}
