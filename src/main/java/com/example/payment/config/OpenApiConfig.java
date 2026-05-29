package com.example.payment.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Payment Orchestration API",
                version = "1.0",
                description = "API documentation for the Payment Orchestration Service"
        )
)
public class OpenApiConfig {
}
