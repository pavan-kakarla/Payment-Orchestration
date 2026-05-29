package com.example.payment.repository;

import com.example.payment.model.ProviderAttempt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProviderAttemptRepository extends JpaRepository<ProviderAttempt, Long> {
    List<ProviderAttempt> findByPaymentIdOrderByIdAsc(String paymentId);
}

