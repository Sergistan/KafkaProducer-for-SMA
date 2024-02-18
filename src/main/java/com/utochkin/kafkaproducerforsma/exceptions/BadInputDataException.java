package com.utochkin.kafkaproducerforsma.exceptions;

public class BadInputDataException extends RuntimeException{
    public BadInputDataException(String msg) {
        super(msg);
    }
}
