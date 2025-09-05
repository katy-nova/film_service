package com.example.films.dto.film;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import com.example.films.dto.GenreDto;
import com.example.films.dto.MpaDto;

import java.time.LocalDate;
import java.util.Set;

@Data
public class NewFilmDto {

    private Long id;
    private String name;
    private String description;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate releaseDate;
    private Set<GenreDto> genres;
    private Integer duration;
    private MpaDto mpa;
}
