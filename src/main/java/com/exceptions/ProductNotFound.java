package com.exceptions;

public class ProductNotFound extends Exception{
    public ProductNotFound() {
        super("The product doesn't exist");
    }

    public ProductNotFound(String message) {
        super(message);
    }
}
