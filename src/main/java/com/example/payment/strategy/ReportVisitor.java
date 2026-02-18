package com.example.payment.strategy;

import com.example.payment.service.InventoryReport;
import com.example.payment.service.SalesReport;

public interface ReportVisitor {

    void visit(SalesReport salesReport);
    void visit(InventoryReport inventoryReport);
}
