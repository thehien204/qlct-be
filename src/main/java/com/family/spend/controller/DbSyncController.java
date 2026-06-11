package com.family.spend.controller;

import com.family.spend.model.Expense;
import com.family.spend.model.Member;
import com.family.spend.model.Payment;
import com.family.spend.repository.ExpenseRepository;
import com.family.spend.repository.MemberRepository;
import com.family.spend.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/db")
public class DbSyncController {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getDatabase() {
        Map<String, Object> db = new HashMap<>();
        db.put("members", memberRepository.findAll());
        db.put("expenses", expenseRepository.findAll());
        db.put("payments", paymentRepository.findAll());
        return ResponseEntity.ok(db);
    }

    @PostMapping
    @Transactional
    public ResponseEntity<Map<String, Object>> syncDatabase(@RequestBody Map<String, Object> payload) {
        Map<String, Object> response = new HashMap<>();
        try {
            // Collect incoming IDs
            List<?> rawMembers = (List<?>) payload.getOrDefault("members", Collections.emptyList());
            Set<String> incomingMemberIds = new HashSet<>();
            for (Object obj : rawMembers) {
                Map<?, ?> map = (Map<?, ?>) obj;
                incomingMemberIds.add((String) map.get("id"));
            }

            List<?> rawExpenses = (List<?>) payload.getOrDefault("expenses", Collections.emptyList());
            Set<String> incomingExpenseIds = new HashSet<>();
            for (Object obj : rawExpenses) {
                Map<?, ?> map = (Map<?, ?>) obj;
                incomingExpenseIds.add((String) map.get("id"));
            }

            List<?> rawPayments = (List<?>) payload.getOrDefault("payments", Collections.emptyList());
            Set<String> incomingPaymentIds = new HashSet<>();
            for (Object obj : rawPayments) {
                Map<?, ?> map = (Map<?, ?>) obj;
                incomingPaymentIds.add((String) map.get("id"));
            }

            // Step 1: Delete Payments not in the incoming payload
            List<Payment> allPayments = paymentRepository.findAll();
            for (Payment p : allPayments) {
                if (!incomingPaymentIds.contains(p.getId())) {
                    paymentRepository.delete(p);
                }
            }

            // Step 2: Delete Expenses not in the incoming payload
            List<Expense> allExpenses = expenseRepository.findAll();
            for (Expense exp : allExpenses) {
                if (!incomingExpenseIds.contains(exp.getId())) {
                    expenseRepository.delete(exp);
                }
            }

            // Step 3: Delete Members not in the incoming payload (validate remaining balance)
            List<Member> allMembers = memberRepository.findAll();
            for (Member m : allMembers) {
                if (!incomingMemberIds.contains(m.getId())) {
                    String mId = m.getId();
                    double balance = 0.0;
                    
                    // 1. Calculate from remaining expenses in DB
                    List<Expense> remainingExpenses = expenseRepository.findAll();
                    for (Expense e : remainingExpenses) {
                        if (mId.equals(e.getPaidById())) {
                            balance += e.getAmount();
                        }
                        List<String> bens = e.getBeneficiaryIds();
                        if (bens != null && bens.contains(mId)) {
                            balance -= e.getAmount() / bens.size();
                        }
                    }
                    
                    // 2. Calculate from remaining settled payments in DB
                    List<Payment> remainingPayments = paymentRepository.findAll();
                    for (Payment p : remainingPayments) {
                        if (p.getIsSettled() != null && p.getIsSettled()) {
                            if (mId.equals(p.getFromId())) {
                                balance += p.getAmount();
                            }
                            if (mId.equals(p.getToId())) {
                                balance -= p.getAmount();
                            }
                        }
                    }
                    
                    if (Math.abs(balance) > 10.0) {
                        throw new RuntimeException("Không thể xoá thành viên " + m.getName() + " vì họ vẫn còn khoản nợ chưa tất toán (Số dư: " + String.format(Locale.US, "%,.0f", balance).replace(",", ".") + " VND). Vui lòng tất toán trước khi xoá.");
                    }
                    memberRepository.delete(m);
                }
            }

            // Step 4: Save / Update Members
            for (Object obj : rawMembers) {
                Map<?, ?> map = (Map<?, ?>) obj;
                String id = (String) map.get("id");
                String name = (String) map.get("name");
                String role = (String) map.get("role");
                String avatarColor = (String) map.get("avatarColor");
                String messengerLink = (String) map.get("messengerLink");
                String messengerId = (String) map.get("messengerId");
                String passcode = (String) map.get("passcode");

                Optional<Member> existingOpt = memberRepository.findById(id);
                Member m;
                if (existingOpt.isPresent()) {
                    m = existingOpt.get();
                    m.setName(name);
                    m.setRole(role);
                    m.setAvatarColor(avatarColor);
                    m.setMessengerLink(messengerLink);
                    m.setMessengerId(messengerId);
                    if (passcode != null && !passcode.trim().isEmpty()) {
                        m.setPasscode(passcode);
                    }
                } else {
                    m = new Member(id, name, role, avatarColor, messengerLink, messengerId, passcode);
                }
                memberRepository.save(m);
            }

            // Step 5: Save / Update Expenses
            for (Object obj : rawExpenses) {
                Map<?, ?> map = (Map<?, ?>) obj;
                String id = (String) map.get("id");
                String title = (String) map.get("title");
                Number amountNum = (Number) map.get("amount");
                Double amount = amountNum != null ? amountNum.doubleValue() : 0.0;
                String categoryId = (String) map.get("categoryId");
                String date = (String) map.get("date");
                String paidById = (String) map.get("paidById");
                String notes = (String) map.get("notes");
                Number createdAtNum = (Number) map.get("createdAt");
                Long createdAt = createdAtNum != null ? createdAtNum.longValue() : System.currentTimeMillis();
                
                List<?> rawBeneficiaryIds = (List<?>) map.get("beneficiaryIds");
                List<String> beneficiaryIds = new ArrayList<>();
                if (rawBeneficiaryIds != null) {
                    for (Object bId : rawBeneficiaryIds) {
                        beneficiaryIds.add(bId.toString());
                    }
                }

                Expense exp = new Expense(id, title, amount, categoryId, date, paidById, notes, createdAt, beneficiaryIds);
                expenseRepository.save(exp);
            }

            // Step 6: Save / Update Payments
            for (Object obj : rawPayments) {
                Map<?, ?> map = (Map<?, ?>) obj;
                String id = (String) map.get("id");
                String month = (String) map.get("month");
                String fromId = (String) map.get("fromId");
                String toId = (String) map.get("toId");
                Number amountNum = (Number) map.get("amount");
                Double amount = amountNum != null ? amountNum.doubleValue() : 0.0;
                Object isSettledVal = map.get("isSettled");
                Boolean isSettled = (isSettledVal instanceof Boolean) ? (Boolean) isSettledVal : true;
                Number createdAtNum = (Number) map.get("createdAt");
                Long createdAt = createdAtNum != null ? createdAtNum.longValue() : System.currentTimeMillis();

                Payment p = new Payment(id, month, fromId, toId, amount, isSettled, createdAt);
                paymentRepository.save(p);
            }

            response.put("success", true);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
}
