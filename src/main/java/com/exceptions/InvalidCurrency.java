package com.exceptions;

public class InvalidCurrency extends Exception{
    public InvalidCurrency() {
        super("The currency you inserted is invalid!");
    }
}
