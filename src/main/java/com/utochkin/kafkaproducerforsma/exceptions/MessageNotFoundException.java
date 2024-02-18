package com.utochkin.kafkaproducerforsma.exceptions;

public class MessageNotFoundException extends RuntimeException{
    public MessageNotFoundException() {
        super("Error: message not found!");
    }
}