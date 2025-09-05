package com.example.films.dto.user;

import lombok.Data;
import com.example.films.dto.film.FilmSimpleDto;
import com.example.films.dto.review.ReviewDto;
import com.example.films.model.enums.Role;

import java.time.LocalDate;
import java.util.Set;

//Здесь можно использовать аннотацию Дата?
@Data
public class UserDto {

    private String login;

    private String email;

    private String name;

    private LocalDate birthday;

    private boolean enabled;

    private Set<ReviewDto> reviews;

    private Set<UserSimpleDto> friends;

    private Set<FilmSimpleDto> likedFilms;

    private Set<Role> roles;
}
