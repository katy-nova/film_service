package com.example.films.dto.film;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import com.example.films.dto.GenreDto;
import com.example.films.dto.MpaDto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

@Data
@AllArgsConstructor
public class FilmListDto {
    private String name;
    private String description;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate releaseDate;
    private Set<GenreDto> genres;
    private BigDecimal rating;
    private Integer duration;
    private MpaDto mpa;
    private int numberOfReviews;
    private int numberOfLikes;
}
