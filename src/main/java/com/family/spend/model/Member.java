package com.family.spend.model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "members")
public class Member {
    @Id
    private String id;
    private String name;
    private String role;
    private String avatarColor;
    private String messengerLink;
    private String messengerId;

    public Member() {
    }

    public Member(String id, String name, String role, String avatarColor, String messengerLink, String messengerId) {
        this.id = id;
        this.name = name;
        this.role = role;
        this.avatarColor = avatarColor;
        this.messengerLink = messengerLink;
        this.messengerId = messengerId;
    }

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
}
