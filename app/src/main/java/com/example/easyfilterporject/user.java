package com.example.easyfilterporject;


public class user {


    private String id;
    private String email;

    public user() {
        // Construtor vazio para o Firebase
    }

    public user(String email) {
        this.email = email;
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