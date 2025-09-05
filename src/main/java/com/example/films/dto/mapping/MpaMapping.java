package com.example.films.dto.mapping;

import org.mapstruct.Mapper;
import com.example.films.dto.MpaDto;
import com.example.films.model.entity.Mpa;

@Mapper(componentModel = "spring")
public interface MpaMapping {

    Mpa toMPA(MpaDto mpaDto);

    MpaDto toMpaDto(Mpa mpa);
}
