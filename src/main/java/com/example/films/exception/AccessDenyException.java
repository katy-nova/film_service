package com.example.films.exception;

public class AccessDenyException extends RuntimeException {
    public AccessDenyException(String message) {
        super(message);
    }
}
