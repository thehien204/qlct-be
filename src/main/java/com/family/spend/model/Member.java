package com.family.spend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import javax.persistence.*;

@Entity
@Table(name = "member")
public class Member {
    @Id
    @Column(length = 50)
    private String id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 50)
    private String role;

    @Column(name = "avatar_color", length = 50)
    private String avatarColor;

    @Column(name = "messenger_link", length = 255)
    private String messengerLink;

    @Column(name = "messenger_id", length = 100)
    private String messengerId;

    @Column(length = 50)
    private String passcode = "123456"; // Default passcode for login

    // Constructors
    public Member() {}

    public Member(String id, String name, String role, String avatarColor, String messengerLink, String messengerId, String passcode) {
        this.id = id;
        this.name = name;
        this.role = role;
        this.avatarColor = avatarColor;
        this.messengerLink = messengerLink;
        this.messengerId = messengerId;
        if (passcode != null && !passcode.trim().isEmpty()) {
            this.passcode = passcode;
        }
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getAvatarColor() {
        return avatarColor;
    }

    public void setAvatarColor(String avatarColor) {
        this.avatarColor = avatarColor;
    }

    public String getMessengerLink() {
        return messengerLink;
    }

    public void setMessengerLink(String messengerLink) {
        this.messengerLink = messengerLink;
    }

    public String getMessengerId() {
        return messengerId;
    }

    public void setMessengerId(String messengerId) {
        this.messengerId = messengerId;
    }

    public String getPasscode() {
        return passcode;
    }

    public void setPasscode(String passcode) {
        this.passcode = passcode;
    }
}
