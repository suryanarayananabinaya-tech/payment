package com.example.payment.strategy;

public interface Report {

    void accept(ReportVisitor reportVisitor);
}
