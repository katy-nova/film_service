package com.example.films.dto.film;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import com.example.films.dto.GenreDto;
import com.example.films.dto.MpaDto;
import com.example.films.dto.review.ReviewForFilmDto;
import com.example.films.dto.user.UserSimpleDto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

@Data
public class FilmDto {
    // не ставлю никаких аннотаций тк это ДТО нужно для передачи из таблицы на фронт
    private String name;
    private String description;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate releaseDate;
    private Set<GenreDto> genres;
    private BigDecimal rating;
    private Integer duration;
    private MpaDto mpa;
    private Set<ReviewForFilmDto> reviews;
    private Set<UserSimpleDto> likedBy;
}
