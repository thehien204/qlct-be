package com.family.spend.service;

import com.family.spend.dto.DbDataDto;
import com.family.spend.model.Expense;
import com.family.spend.model.Member;
import com.family.spend.model.Payment;
import com.family.spend.repository.ExpenseRepository;
import com.family.spend.repository.MemberRepository;
import com.family.spend.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DbSyncService {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    /**
     * Replaces the entire database content in a transaction.
     */
    @Transactional
    public void syncData(DbDataDto data) {
        // Clear all existing data
        memberRepository.deleteAllInBatch();
        expenseRepository.deleteAllInBatch();
        paymentRepository.deleteAllInBatch();

        // Save new data
        if (data.getMembers() != null && !data.getMembers().isEmpty()) {
            memberRepository.saveAll(data.getMembers());
        }
        if (data.getExpenses() != null && !data.getExpenses().isEmpty()) {
            expenseRepository.saveAll(data.getExpenses());
        }
        if (data.getPayments() != null && !data.getPayments().isEmpty()) {
            paymentRepository.saveAll(data.getPayments());
        }
    }
}
