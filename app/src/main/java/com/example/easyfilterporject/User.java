package com.example.easyfilterporject;

public class User {

    private String name;
    private String id;
    private String email;
    private boolean isBlocked;

    public User() {

    }
     public User(String name, String email, boolean isBlocked) {
        this.name = name;
        this.email = email;
        this.isBlocked = false;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
    public boolean isBlocked() {
        return isBlocked;
    }

    public void setBlocked(boolean blocked) {
        isBlocked = blocked;
    }
}