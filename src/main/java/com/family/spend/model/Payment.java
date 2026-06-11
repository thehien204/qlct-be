package com.family.spend.model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "payments")
public class Payment {
    @Id
    private String id;
    private String month;
    private String fromId;
    private String toId;
    private Double amount;
    private Boolean isSettled;
    private Long createdAt;

    public Payment() {
    }

    public Payment(String id, String month, String fromId, String toId, Double amount, Boolean isSettled, Long createdAt) {
        this.id = id;
        this.month = month;
        this.fromId = fromId;
        this.toId = toId;
        this.amount = amount;
        this.isSettled = isSettled;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public String getFromId() {
        return fromId;
    }

    public void setFromId(String fromId) {
        this.fromId = fromId;
    }

    public String getToId() {
        return toId;
    }

    public void setToId(String toId) {
        this.toId = toId;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public Boolean getIsSettled() {
        return isSettled;
    }

    public void setIsSettled(Boolean settled) {
        isSettled = settled;
    }

    public Long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Long createdAt) {
        this.createdAt = createdAt;
    }
}
