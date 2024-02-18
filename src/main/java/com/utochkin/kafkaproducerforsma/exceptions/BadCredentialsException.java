package com.utochkin.kafkaproducerforsma.exceptions;

public class BadCredentialsException extends RuntimeException{
    public BadCredentialsException() {
        super("Incorrect username and/or password");
    }
}
