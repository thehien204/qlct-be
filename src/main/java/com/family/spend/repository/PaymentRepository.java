package com.family.spend.repository;

import com.family.spend.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, String> {
    boolean existsByFromIdOrToId(String fromId, String toId);
}
