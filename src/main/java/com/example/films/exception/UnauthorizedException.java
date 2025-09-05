package com.example.films.exception;

public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException() {
        super("Для совершения данного запроса необходима авторизация");
    }
}
