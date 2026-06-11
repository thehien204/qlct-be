package com.family.spend.model;

import javax.persistence.*;

@Entity
@Table(name = "payment")
public class Payment {
    @Id
    @Column(length = 50)
    private String id;

    @Column(nullable = false, length = 20)
    private String month; // YYYY-MM

    @Column(name = "from_id", nullable = false, length = 50)
    private String fromId;

    @Column(name = "to_id", nullable = false, length = 50)
    private String toId;

    @Column(nullable = false)
    private Double amount;

    @Column(name = "is_settled", nullable = false)
    private Boolean isSettled = true;

    @Column(name = "created_at", nullable = false)
    private Long createdAt;

    // Constructors
    public Payment() {}

    public Payment(String id, String month, String fromId, String toId, Double amount, Boolean isSettled, Long createdAt) {
        this.id = id;
        this.month = month;
        this.fromId = fromId;
        this.toId = toId;
        this.amount = amount;
        this.isSettled = isSettled;
        this.createdAt = createdAt;
    }

    // Getters and Setters
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

    public void setIsSettled(Boolean isSettled) {
        this.isSettled = isSettled;
    }

    public Long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Long createdAt) {
        this.createdAt = createdAt;
    }
}
