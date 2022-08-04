package com.exceptions;

public class NotEnoughMoney extends Exception{
    public NotEnoughMoney() {
        super("Not enough money is inserted! Try another product or insert more money!");
    }
    public NotEnoughMoney(String message){
        super(message);
    }
}
