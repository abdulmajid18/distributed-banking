package com.bank.pulsar.exception;

public class MessageServiceException extends Exception {
    public MessageServiceException(String message) {
        super(message);
    }

    public MessageServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}