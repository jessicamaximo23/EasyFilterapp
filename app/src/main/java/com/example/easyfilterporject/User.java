package com.example.easyfilterporject;

public class User {

    private String name;
    private String id;
    private String email;

    public User() {

    }
     public User(String name, String email) {
        this.name = name;
        this.email = email;
    }


    public User(String id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
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
}