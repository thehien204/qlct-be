package com.family.spend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Entity
@Table(name = "expenses")
public class Expense {
    @Id
    private String id;
    private String title;
    private Double amount;
    private String categoryId;
    private String date;
    private String paidById;

    @Column(length = 4000)
    private String notes;
    private Long createdAt;

    @JsonIgnore
    @Column(name = "beneficiary_ids", length = 2000)
    private String beneficiaryIdsStr;

    public Expense() {
    }

    @JsonProperty("beneficiaryIds")
    public List<String> getBeneficiaryIds() {
        if (beneficiaryIdsStr == null || beneficiaryIdsStr.trim().isEmpty()) {
            return new ArrayList<>();
        }
        return Arrays.asList(beneficiaryIdsStr.split(","));
    }

    @JsonProperty("beneficiaryIds")
    public void setBeneficiaryIds(List<String> ids) {
        if (ids == null || ids.isEmpty()) {
            this.beneficiaryIdsStr = "";
        } else {
            this.beneficiaryIdsStr = String.join(",", ids);
        }
    }

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

    public String getBeneficiaryIdsStr() {
        return beneficiaryIdsStr;
    }

    public void setBeneficiaryIdsStr(String beneficiaryIdsStr) {
        this.beneficiaryIdsStr = beneficiaryIdsStr;
    }
}
