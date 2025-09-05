package com.example.films.dto.mapping;

import org.mapstruct.Mapper;
import com.example.films.dto.GenreDto;
import com.example.films.model.entity.Genre;

@Mapper(componentModel = "spring")
public interface GenreMapping {

    GenreDto toDto(Genre genre);

    Genre fromDto(GenreDto genreDto);
}
