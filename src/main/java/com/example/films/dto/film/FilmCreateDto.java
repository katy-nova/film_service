package com.example.films.dto.film;

import jakarta.validation.constraints.NotBlank;
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
public class FilmCreateDto {
    // отдельное ДТО для создания, чтобы поле рейтинг невозможно было задать вручную

    @NotBlank(message = "Это поле обязательно для заполнения")
    private String name;

    @Size(max = 200, message = "Длина описания не должна превышать 200 символов")
    private String description;

    @Past(message = "Дата релиза должна быть в прошлом")
    @MinReleaseDate
    private LocalDate releaseDate;

    private Set<GenreDto> genres;

    private MpaDto mpa;

    @Positive(message = "Длительность фильма не может быть отрицательной")
    private int duration;
}
