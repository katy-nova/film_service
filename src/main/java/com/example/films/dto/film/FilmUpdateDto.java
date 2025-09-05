package com.example.films.dto.film;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;
import com.example.films.dto.GenreDto;
import com.example.films.dto.MpaDto;
import com.example.films.validation.MinReleaseDate;

import java.time.LocalDate;
import java.util.Set;

@Data
public class FilmUpdateDto {
    @Nullable
    private String name;

    @Nullable
    @Size(max = 200, message = "Длина описания не должна превышать 200 символов")
    private String description;

    @Nullable
    @Past(message = "Дата релиза должна быть в прошлом")
    @MinReleaseDate
    private LocalDate releaseDate;

    @Nullable
    private Set<GenreDto> genres;

    @Nullable
    private MpaDto mpa;

    @Positive(message = "Длительность фильма не может быть отрицательной")
    @Nullable
    private Integer duration;

}

