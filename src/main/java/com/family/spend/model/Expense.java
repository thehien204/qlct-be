package com.family.spend.model;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "expense")
public class Expense {
    @Id
    @Column(length = 50)
    private String id;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(nullable = false)
    private Double amount;

    @Column(name = "category_id", nullable = false, length = 50)
    private String categoryId;

    @Column(nullable = false, length = 20)
    private String date; // YYYY-MM-DD

    @Column(name = "paid_by_id", nullable = false, length = 50)
    private String paidById;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_at", nullable = false)
    private Long createdAt;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "expense_beneficiary",
        joinColumns = @JoinColumn(name = "expense_id")
    )
    @Column(name = "member_id", length = 50)
    private List<String> beneficiaryIds = new ArrayList<>();

    // Constructors
    public Expense() {}

    public Expense(String id, String title, Double amount, String categoryId, String date, String paidById, String notes, Long createdAt, List<String> beneficiaryIds) {
        this.id = id;
        this.title = title;
        this.amount = amount;
        this.categoryId = categoryId;
        this.date = date;
        this.paidById = paidById;
        this.notes = notes;
        this.createdAt = createdAt;
        if (beneficiaryIds != null) {
            this.beneficiaryIds = beneficiaryIds;
        }
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getPaidById() {
        return paidById;
    }

    public void setPaidById(String paidById) {
        this.paidById = paidById;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Long createdAt) {
        this.createdAt = createdAt;
    }

    public List<String> getBeneficiaryIds() {
        return beneficiaryIds;
    }

    public void setBeneficiaryIds(List<String> beneficiaryIds) {
        this.beneficiaryIds = beneficiaryIds;
    }
}
