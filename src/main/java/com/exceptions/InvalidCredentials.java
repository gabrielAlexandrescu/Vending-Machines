package com.exceptions;

public class InvalidCredentials extends Exception{
    public InvalidCredentials(String message) {
        super("Invalid credentials! "+message);
    }
}
