package com.example.payment.service;


import com.example.payment.decorator.PaymentLogging;
import com.example.payment.dto.PaymentRequestDTO;
import com.example.payment.dto.PaymentResponseDTO;
import com.example.payment.factory.PaymentFactory;
import com.example.payment.model.PaymentRequest;
import com.example.payment.observer.PaymentEvent;
import com.example.payment.proxy.PaymentProxy;
import com.example.payment.strategy.PaymentStrategy;
import com.example.payment.strategy.Report;
import com.example.payment.strategy.ReportVisitor;
import com.example.payment.template.DefaultPaymentProcessor;
import com.example.payment.template.PaymentProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PaymentService {


    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    PaymentStrategy paymentStrategy;


    public PaymentResponseDTO processPayment(PaymentRequestDTO requestDTO) {

        // =====================
        // Build Immutable Model
        // =====================
        PaymentRequest request = PaymentRequest.builder()
                .amount(requestDTO.getAmount())
                .currency(requestDTO.getCurrency())
                .email(requestDTO.getEmail())
                .paymentType(requestDTO.getPaymentType()).build();

        try{
            paymentStrategy = PaymentFactory.getStrategy(request.getPaymentType());
        }catch (IllegalArgumentException e){
            return new PaymentResponseDTO("FAILED","Invalid Payment type", request.getAmount(),  request.getPaymentType());
        }

        paymentStrategy = new PaymentProxy(paymentStrategy);

        paymentStrategy = new PaymentLogging(paymentStrategy);

        PaymentProcessor processor = new DefaultPaymentProcessor(paymentStrategy);
        processor.processPayment(request);

        // observer for Notifications
        applicationEventPublisher.publishEvent(new PaymentEvent(request));

        // 7️⃣ Return API Response
        return new PaymentResponseDTO(
                "SUCCESS",
                "Payment processed successfully",
                request.getAmount(),
                request.getPaymentType()
        );
    }


    public void generateReport() {
        List<Report> reports = List.of(new SalesReport(), new InventoryReport());
        //pdf//
        ReportVisitor pdfReportVisitor = new PDFReportVisitor();
        reports.forEach(report -> {report.accept(pdfReportVisitor);});

        //csv//
        ReportVisitor csvReportVisitor = new CSVReportVisitor();
        reports.forEach(report -> {report.accept(csvReportVisitor);});

    }
}
