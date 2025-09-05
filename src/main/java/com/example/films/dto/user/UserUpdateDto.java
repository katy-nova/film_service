package com.example.films.dto.user;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UserUpdateDto {

    @Nullable
    @Pattern(regexp = "^\\S*$", message = "Поле не должно содержать пробелы")
    private String login;

    @Nullable
    @Email(message = "Неверный формат email")
    private String email;

    @Nullable
    private String name;

    @Nullable
    @Past(message = "Дата рождения должна быть в прошлом")
    private LocalDate birthday;

    @Nullable
    @Pattern(regexp = "^\\S*$", message = "Пароль не должен содержать пробелы")
    private String password;
}
