package com.exceptions;

public class TooManyProducts extends Exception{
    public TooManyProducts() {
        super("There are too many products on the rack; impossible operation");
    }
}
