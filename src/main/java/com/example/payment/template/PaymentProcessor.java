package com.example.payment.template;

import com.example.payment.model.PaymentRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class PaymentProcessor {

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    public final void processPayment(PaymentRequest request){
        validate(request);      // Step 1: Validate
        executePayment(request); // Step 2: Execute payment
        postProcess(request);
    }

    protected void validate(PaymentRequest request) {
        logger.info("Template Method: Validating payment request...");
    }

    protected abstract void executePayment(PaymentRequest request);

    protected void postProcess(PaymentRequest request) {
        logger.info("Template Method: Post-processing payment...");
    }

}
