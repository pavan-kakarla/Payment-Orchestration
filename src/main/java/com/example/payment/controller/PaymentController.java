package com.example.payment.controller;

import com.example.payment.service.PaymentService;
import com.example.payment.service.dto.CreatePaymentRequest;
import com.example.payment.service.dto.CreatePaymentResponse;
import com.example.payment.service.dto.FetchPaymentResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping
    public ResponseEntity<CreatePaymentResponse> createPayment(
            @RequestBody CreatePaymentRequest req) {
        CreatePaymentResponse resp = paymentService.createPayment(req);
        return ResponseEntity.status(resp.getHttpStatus()).body(resp);
    }

    @GetMapping("/{id}")
    public ResponseEntity<FetchPaymentResponse> fetchPayment(@PathVariable("id") String id) {
        FetchPaymentResponse resp = paymentService.fetchPayment(id);
        if (resp == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(resp);
    }
}