package com.example.films.controller.films;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Sql(scripts = "/films_for_spec_data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/films_for_spec_cleanup.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public class FilmControllerSpecificationIntegrationTest {

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

    @Test
    void shouldGetFilmsByGenre() throws Exception {
        mockMvc.perform(get("/films?genre=COMEDY"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(3)))
                .andExpect(jsonPath("$.content[*].name", hasItem("Comedy2000_1")))
                .andExpect(jsonPath("$.content[*].name", hasItem("Comedy2005_2")))
                .andExpect(jsonPath("$.content[*].name", hasItem("ComedyAction2000_1")));
    }

    @Test
    void shouldGetFilmsBy2Genres() throws Exception {
        mockMvc.perform(get("/films?genre=COMEDY&genre=ACTION"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(5)))
                .andExpect(jsonPath("$.content[*].name", hasItem("Comedy2000_1")))
                .andExpect(jsonPath("$.content[*].name", hasItem("Comedy2005_2")))
                .andExpect(jsonPath("$.content[*].name", hasItem("ComedyAction2000_1")))
                .andExpect(jsonPath("$.content[*].name", hasItem("Action2000_2")))
                .andExpect(jsonPath("$.content[*].name", hasItem("Action2003_3")));
    }

    @Test
    void shouldGetFilmsByName() throws Exception {
        mockMvc.perform(get("/films?name=com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(3)))
                .andExpect(jsonPath("$.content[*].name", hasItem("Comedy2000_1")))
                .andExpect(jsonPath("$.content[*].name", hasItem("Comedy2005_2")))
                .andExpect(jsonPath("$.content[*].name", hasItem("ComedyAction2000_1")));
    }

    @Test
    void shouldGetFilmsByMpa() throws Exception {
        mockMvc.perform(get("/films?mpa=G"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[*].name", hasItem("Comedy2000_1")))
                .andExpect(jsonPath("$.content[*].name", hasItem("ComedyAction2000_1")));
    }

    @Test
    void shouldGetFilmsBy2Mpa() throws Exception {
        mockMvc.perform(get("/films?mpa=G&mpa=PG"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(4)))
                .andExpect(jsonPath("$.content[*].name", hasItem("Comedy2000_1")))
                .andExpect(jsonPath("$.content[*].name", hasItem("Comedy2005_2")))
                .andExpect(jsonPath("$.content[*].name", hasItem("ComedyAction2000_1")))
                .andExpect(jsonPath("$.content[*].name", hasItem("Action2000_2")));
    }

    @Test
    void shouldGetFilmsByRating() throws Exception {
        mockMvc.perform(get("/films?rating=8"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[*].name", hasItem("Comedy2000_1")))
                .andExpect(jsonPath("$.content[*].name", hasItem("ComedyAction2000_1")));
    }

    @Test
    void shouldGetFilmsByFromYear() throws Exception {
        mockMvc.perform(get("/films?fromYear=2002"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[*].name", hasItem("Comedy2005_2")))
                .andExpect(jsonPath("$.content[*].name", hasItem("Action2003_3")));
    }

    @Test
    void shouldGetFilmsByToYear() throws Exception {
        mockMvc.perform(get("/films?toYear=2001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(3)))
                .andExpect(jsonPath("$.content[*].name", hasItem("Comedy2000_1")))
                .andExpect(jsonPath("$.content[*].name", hasItem("ComedyAction2000_1")))
                .andExpect(jsonPath("$.content[*].name", hasItem("Action2000_2")));
    }

    @Test
    void shouldGetFilmsByFromYearAndToYear() throws Exception {
        mockMvc.perform(get("/films?toYear=2004&fromYear=2001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].name").value("Action2003_3"));
    }

    @Test
    void shouldNotGetFilmsWithIncorrectYears() throws Exception {
        mockMvc.perform(get("/films?toYear=2001&fromYear=2004"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Неверно заданы года")));
    }

    @Test
    void shouldGetFilmsByGenreAndRating() throws Exception {
        mockMvc.perform(get("/films?genre=COMEDY&rating=8"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[*].name", hasItem("Comedy2000_1")))
                .andExpect(jsonPath("$.content[*].name", hasItem("ComedyAction2000_1")));
    }

    @Test
    void shouldGetFilmsByGenreAndRatingAndYear() throws Exception {
        mockMvc.perform(get("/films?genre=ACTION&rating=7&toYear=2002"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].name").value("ComedyAction2000_1"));
    }
}
