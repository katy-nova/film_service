package com.example.films.dto.film;

import lombok.Data;
import com.example.films.dto.GenreDto;

import java.util.Set;

@Data
public class FilmSimpleDto {
    private String name;
    private Set<GenreDto> genre;
}
