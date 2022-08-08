package com.exceptions;

public class InvalidProductType extends Exception{
    public InvalidProductType(String message) {
        super("Invalid product type! Type " + message + " was expected!");
    }
}
