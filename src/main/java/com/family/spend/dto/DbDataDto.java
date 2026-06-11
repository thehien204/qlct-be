package com.family.spend.dto;

import com.family.spend.model.Expense;
import com.family.spend.model.Member;
import com.family.spend.model.Payment;

import java.util.List;

public class DbDataDto {
    private List<Member> members;
    private List<Expense> expenses;
    private List<Payment> payments;

    public DbDataDto() {
    }

    public DbDataDto(List<Member> members, List<Expense> expenses, List<Payment> payments) {
        this.members = members;
        this.expenses = expenses;
        this.payments = payments;
    }

    public List<Member> getMembers() {
        return members;
    }

    public void setMembers(List<Member> members) {
        this.members = members;
    }

    public List<Expense> getExpenses() {
        return expenses;
    }

    public void setExpenses(List<Expense> expenses) {
        this.expenses = expenses;
    }

    public List<Payment> getPayments() {
        return payments;
    }

    public void setPayments(List<Payment> payments) {
        this.payments = payments;
    }
}
