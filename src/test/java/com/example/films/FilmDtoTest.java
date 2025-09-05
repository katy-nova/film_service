package com.example.films;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import com.example.films.dto.film.FilmDto;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FilmDtoTest {

    private final ObjectMapper objectMapper = new ObjectMapper();


    @Test
    public void testSerialization() throws Exception {
        // тесты работают только если здесь вручную зарегистрировать модуль
        objectMapper.registerModule(new JavaTimeModule());
        FilmDto filmDto = new FilmDto();
        filmDto.setReleaseDate(LocalDate.of(2023, 10, 1));

        String jsonString = objectMapper.writeValueAsString(filmDto);
        System.out.println(jsonString);
    }

    @Test
    public void testDeserialization() throws Exception {
        objectMapper.registerModule(new JavaTimeModule());
        String jsonString = "{\"releaseDate\":\"2023-10-01\"}";

        FilmDto filmDto = objectMapper.readValue(jsonString, FilmDto.class);
        assertEquals(LocalDate.of(2023, 10, 1), filmDto.getReleaseDate());
    }
}

