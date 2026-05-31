package com.example.payment.camel;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class PaymentRoute extends RouteBuilder {

    @Override
    public void configure() throws Exception {
        // Error handling for the entire route
        errorHandler(deadLetterChannel("direct:paymentFailure")
            .maximumRedeliveries(3) // Retry up to 3 times
            .redeliveryDelay(2000) // 2 seconds delay between retries
            .retryAttemptedLogLevel(org.apache.camel.LoggingLevel.WARN));

        // Main route for payment processing
        from("direct:processPayment")
            .routeId("PaymentProcessingRoute")
            .log("Processing payment for body: ${body}")
            .to("bean:paymentProcessor?method=process") // Changed to use bean component
            .choice()
                .when(simple("${body.status} == 'SUCCESS'"))
                    .to("direct:paymentSuccess")
                .otherwise()
                    .to("direct:paymentFailure");

        // Payment Success Handler
        from("direct:paymentSuccess")
            .routeId("PaymentSuccessHandler")
            .log("Payment successful for body: ${body}")
            .to("bean:paymentSuccessHandler?method=handleSuccess"); // Call a Spring bean method

        // Payment Failure Handler
        from("direct:paymentFailure")
            .routeId("PaymentFailureHandler")
            .log("Payment failed for body: ${body}")
            .to("bean:paymentFailureHandler?method=handleFailure"); // Call a Spring bean method
    }
}
