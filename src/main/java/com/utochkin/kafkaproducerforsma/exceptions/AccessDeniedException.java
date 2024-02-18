package com.utochkin.kafkaproducerforsma.exceptions;

public class AccessDeniedException extends RuntimeException {

    public AccessDeniedException(String msg) {
        super(msg);
    }
}
