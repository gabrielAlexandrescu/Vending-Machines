package com.domain;

public class Admin extends User {
    private final String password;
    public Admin(String username,String password) {
        super(username);
        this.password = password;
    }
    @Override
    public boolean isAdmin(){
        return true;
    }
}
