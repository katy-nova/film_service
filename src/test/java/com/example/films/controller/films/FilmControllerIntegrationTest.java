package com.example.films.controller.films;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import com.example.films.dto.GenreDto;
import com.example.films.dto.MpaDto;
import com.example.films.dto.film.FilmCreateDto;
import com.example.films.dto.film.FilmUpdateDto;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@WithUserDetails("johndoe")
public class FilmControllerIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:13")
            .withDatabaseName("filmorate_test")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void postgresProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private FilmCreateDto filmCreateDto;
    private FilmUpdateDto filmUpdateDto;

    @BeforeEach
    void setUp() {
        GenreDto genre1 = new GenreDto(1, "COMEDY");
        GenreDto genre2 = new GenreDto(3, "ACTION");
        Set<GenreDto> genreSet = new HashSet<>();
        genreSet.add(genre1);
        genreSet.add(genre2);
        MpaDto mpa = new MpaDto(1, "G");
        filmCreateDto = new FilmCreateDto();
        filmCreateDto.setName("New Film");
        filmCreateDto.setDuration(130);
        filmCreateDto.setGenres(genreSet);
        filmCreateDto.setMpa(mpa);
        filmCreateDto.setDescription("New Film Description");
        filmCreateDto.setReleaseDate(LocalDate.of(2000, 5, 1));
        filmUpdateDto = new FilmUpdateDto();
    }

    @Test
    @WithAnonymousUser
    void shouldGetFilmById() throws Exception {
        mockMvc.perform(get("/films/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("The Shawshank Redemption"));
    }

    @Test
    void createFilm() throws Exception {
        String json = objectMapper.writeValueAsString(filmCreateDto);
        mockMvc.perform(post("/films").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("New Film"))
                .andExpect(jsonPath("$.description").value("New Film Description"))
                .andExpect(jsonPath("$.releaseDate").value("2000-05-01"))
                .andExpect(jsonPath("$.duration").value("130"))
                .andExpect(jsonPath("$.genres", hasSize(2)))
                .andExpect(jsonPath("$.genres[*].name", hasItem("COMEDY")))
                .andExpect(jsonPath("$.genres[*].name", hasItem("ACTION")))
                .andExpect(jsonPath("$.mpa.name").value("G"));
    }

    @Test
    @WithUserDetails("jamesbrown")
    void shouldNotAddFilmWithoutAdminRole() throws Exception {
        String json = objectMapper.writeValueAsString(filmCreateDto);
        mockMvc.perform(post("/films").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldNotAddFilmWithIncorrectDate() throws Exception {
        filmCreateDto.setReleaseDate(LocalDate.of(2200, 5, 1));
        String json = objectMapper.writeValueAsString(filmCreateDto);
        mockMvc.perform(post("/films").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Дата релиза должна быть в прошлом")));

        filmCreateDto.setReleaseDate(LocalDate.of(1720, 5, 1));
        json = objectMapper.writeValueAsString(filmCreateDto);
        mockMvc.perform(post("/films").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Дата не может быть раньше 28 декабря 1895 года")));
    }

    @Test
    void shouldNotAddFilmWithIncorrectDuration() throws Exception {
        filmCreateDto.setDuration(-120);
        String json = objectMapper.writeValueAsString(filmCreateDto);
        mockMvc.perform(post("/films").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Длительность фильма не может быть отрицательной")));
    }

    @Test
    void shouldNotAddFilmWithEmptyName() throws Exception {
        filmCreateDto.setName(null);
        String json = objectMapper.writeValueAsString(filmCreateDto);
        mockMvc.perform(post("/films").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Это поле обязательно для заполнения")));
    }

    @Test
    void shouldNotAddFilmWithTooLongDescription() throws Exception {
        filmCreateDto.setDescription("В далёком будущем, когда человечество освоило космос и начало колонизировать " +
                "новые планеты, группа исследователей сталкивается с загадочной аномалией, угрожающей всей галактике. " +
                "Их миссия — раскрыть тайну и спасти цивилизацию от неминуемой катастрофы, преодолевая невероятные " +
                "трудности и внутренние конфликты.");
        String json = objectMapper.writeValueAsString(filmCreateDto);
        mockMvc.perform(post("/films").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Длина описания не должна превышать 200 символов")));
    }

    @Test
    void updateFilm() throws Exception {
        filmUpdateDto.setName("Updated Film");
        filmUpdateDto.setDescription("Updated Film Description");
        filmUpdateDto.setReleaseDate(LocalDate.of(2005, 5, 1));
        GenreDto genre1 = new GenreDto(7, "DETECTIVE");
        Set<GenreDto> genres = new HashSet<>();
        genres.add(genre1);
        MpaDto mpa = new MpaDto(5, "NC-17");
        filmUpdateDto.setGenres(genres);
        filmUpdateDto.setMpa(mpa);
        filmUpdateDto.setDuration(123);
        String json = objectMapper.writeValueAsString(filmUpdateDto);
        mockMvc.perform(put("/films/1").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Film"))
                .andExpect(jsonPath("$.description").value("Updated Film Description"))
                .andExpect(jsonPath("$.releaseDate").value("2005-05-01"))
                .andExpect(jsonPath("$.duration").value("123"))
                .andExpect(jsonPath("$.genres", hasSize(1)))
                .andExpect(jsonPath("$.genres[0].name").value("DETECTIVE"))
                .andExpect(jsonPath("$.mpa.name").value("NC-17"));

    }

    @Test
    @WithUserDetails("jamesbrown")
    void shouldNotUpdateFilmWithoutAdminRole() throws Exception {
        String json = objectMapper.writeValueAsString(filmUpdateDto);
        mockMvc.perform(put("/films/1").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteFilm() throws Exception {
        mockMvc.perform(delete("/films/1"))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/films/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithUserDetails("jamesbrown")
    void shouldNotDeleteFilmWithoutAdminRole() throws Exception {
        mockMvc.perform(delete("/films/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldGetFilms() throws Exception {
        mockMvc.perform(get("/films"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(5)))
                .andExpect(jsonPath("$.content[*].name", hasItem("Inception")))
                .andExpect(jsonPath("$.content[*].name", hasItem("The Shawshank Redemption")))
                .andExpect(jsonPath("$.content[*].name", hasItem("The Godfather")))
                .andExpect(jsonPath("$.content[*].name", hasItem("The Dark Knight")))
                .andExpect(jsonPath("$.content[*].name", hasItem("Pulp Fiction")));
    }


}
