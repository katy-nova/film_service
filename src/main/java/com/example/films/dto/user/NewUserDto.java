package com.example.films.dto.user;

import lombok.Data;

import java.time.LocalDate;

@Data
public class NewUserDto {

    private Long id;
    private String name;
    private String email;
    private String login;
    private LocalDate birthday;
}
