package com.family.spend.controller;

import com.family.spend.dto.DbDataDto;
import com.family.spend.repository.ExpenseRepository;
import com.family.spend.repository.MemberRepository;
import com.family.spend.repository.PaymentRepository;
import com.family.spend.service.DbSyncService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class DbController {

    private static final Logger log = LoggerFactory.getLogger(DbController.class);

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private DbSyncService dbSyncService;

    /**
     * Retrieves all data from the database.
     */
    @GetMapping("/db")
    public DbDataDto getData() {
        DbDataDto dto = new DbDataDto();
        dto.setMembers(memberRepository.findAll());
        dto.setExpenses(expenseRepository.findAll());
        dto.setPayments(paymentRepository.findAll());
        return dto;
    }

    /**
     * Replaces the local database state.
     */
    @PostMapping("/db")
    public Map<String, Object> syncData(@RequestBody DbDataDto payload) {
        Map<String, Object> response = new HashMap<>();
        try {
            // Synchronize with H2 locally
            dbSyncService.syncData(payload);

            response.put("success", true);
            response.put("message", "Dữ liệu đã được lưu trữ cục bộ thành công!");
        } catch (Exception e) {
            log.error("Error synchronizing data locally: " + e.getMessage(), e);
            response.put("success", false);
            response.put("error", e.getMessage() != null ? e.getMessage() : "Lỗi hệ thống khi lưu trữ dữ liệu.");
        }
        return response;
    }
}
