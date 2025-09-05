package com.example.films.dto.jwt;

import lombok.Data;

@Data
public class UserCredentialDto {
    private String login;
    private String password;
}
