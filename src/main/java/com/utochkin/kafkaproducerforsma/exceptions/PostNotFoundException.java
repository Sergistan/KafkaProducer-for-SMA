package com.utochkin.kafkaproducerforsma.exceptions;

public class PostNotFoundException extends RuntimeException{
    public PostNotFoundException() {
        super("Error: post not found!");
    }
}