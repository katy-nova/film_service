package com.example.films.controller.films;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@Sql(scripts = "/likes_data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/likes_cleanup.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public class FilmControllerLikesIntegrationTest {
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
    void shouldGetPopularFilms() throws Exception {
        mockMvc.perform(get("/films/popular"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(5)))
                .andExpect(jsonPath("$.content[0].name").value("The Shawshank Redemption"))
                .andExpect(jsonPath("$.content[1].name").value("Inception"))
                .andExpect(jsonPath("$.content[2].name").value("The Godfather"))
                .andExpect(jsonPath("$.content[3].name").value("Pulp Fiction"))
                .andExpect(jsonPath("$.content[4].name").value("The Dark Knight"))
                .andExpect(jsonPath("$.totalElements").value(5))
                .andExpect(jsonPath("$.number").value(0))
                .andExpect(jsonPath("$.totalPages").value(1));

        mockMvc.perform(get("/films/popular?count=3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(3)))
                .andExpect(jsonPath("$.content[0].name").value("The Shawshank Redemption"))
                .andExpect(jsonPath("$.content[1].name").value("Inception"))
                .andExpect(jsonPath("$.content[2].name").value("The Godfather"))
                .andExpect(jsonPath("$.totalElements").value(5))
                .andExpect(jsonPath("$.number").value(0))
                .andExpect(jsonPath("$.totalPages").value(2));
    }

    @Test
    @WithUserDetails("johndoe")
    void shouldAddLike() throws Exception {
        mockMvc.perform(put("/films/5/like/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.likedBy").isArray())
                .andExpect(jsonPath("$.likedBy", hasSize(2)))
                .andExpect(jsonPath("$.likedBy[*].login", hasItem("johndoe")));
    }

    @Test
    @Sql(scripts = "/likes_data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "/likes_cleanup.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @WithUserDetails("johndoe")
    void shouldDeleteLike() throws Exception {
        mockMvc.perform(delete("/films/5/like/2"))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/films/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.likedBy").isArray())
                .andExpect(jsonPath("$.likedBy", hasSize(1)))
                .andExpect(jsonPath("$.likedBy[0].login").value("lindawilliams"));
    }
}
