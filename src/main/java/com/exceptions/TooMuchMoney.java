package com.exceptions;

public class TooMuchMoney extends Exception{
    public TooMuchMoney(String message) {
        super("The machine has reached its money limit!"+ message);
    }
}
