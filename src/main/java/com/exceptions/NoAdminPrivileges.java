package com.exceptions;

public class NoAdminPrivileges extends Exception{
    public NoAdminPrivileges(String message) {
        super("Only users with admin privileges can perform that operation! "+ message);
    }
}
