package com.example.films.dto.mapping;

import com.example.films.dto.film.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import com.example.films.dto.film.*;
import com.example.films.model.entity.Film;

@Mapper(componentModel = "spring", uses = ReviewMapping.class)
public interface FilmMapping {

    Film fromDto(FilmDto filmDto);

    FilmDto toDto(Film film);

    Film fromCreateDto(FilmCreateDto filmDto);

    FilmSimpleDto toSimpleDto(Film film);

    NewFilmDto toNewFilmDto(Film film);

    @Mapping(target = "numberOfReviews", expression = "java(film.getReviews().size())")
    @Mapping(target = "numberOfLikes", expression = "java(film.getLikedBy().size())")
    FilmListDto toFilmListDto(Film film);
}
