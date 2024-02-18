package com.utochkin.kafkaproducerforsma.exceptions;

public class ChatNotFoundException extends RuntimeException{
    public ChatNotFoundException() {
        super("Error: chat not found!");
    }
}