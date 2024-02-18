package com.utochkin.kafkaproducerforsma.exceptions;

public class UserNotFoundException extends RuntimeException{
    public UserNotFoundException() {
        super("Error: user not found!");
    }
}