package com.example.films.dto.user;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UserCreateDto {
    @NotBlank(message = "Это поле обязательно для заполнения")
    @Pattern(regexp = "^\\S*$", message = "Поле не должно содержать пробелы")
    private String login;

    @NotBlank(message = "Это поле обязательно для заполнения")
    @Email(message = "Неверный формат email")
    private String email;

    @Nullable
    private String name;

    @Past(message = "Дата рождения должна быть в прошлом")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate birthday;

    @NotBlank(message = "Это поле обязательно для заполнения")
    @Pattern(regexp = "^\\S*$", message = "Пароль не должен содержать пробелы")
    private String password;

}
