package com.example.films.controller.films;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import com.example.films.dto.review.ReviewCreateDto;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class FilmControllerReviewIntegrationTest {

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

    private ReviewCreateDto reviewCreateDto;

    @BeforeEach
    void setUp() {
        reviewCreateDto = new ReviewCreateDto();
        reviewCreateDto.setRating(BigDecimal.TEN);
        reviewCreateDto.setFilmId(1L);
        reviewCreateDto.setUserId(1L);
        reviewCreateDto.setText("Test review");
    }

    @Test
    @WithUserDetails("annasmith")
    void addReview() throws Exception {
        String json = objectMapper.writeValueAsString(reviewCreateDto);

        mockMvc.perform(post("/films/1/review/1").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.rating").value(BigDecimal.TEN))
                .andExpect(jsonPath("$.userName").value("Anna Smith"))
                .andExpect(jsonPath("$.filmName").value("Inception"));

        mockMvc.perform(get("/films/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rating").value(BigDecimal.TEN))
                .andExpect(jsonPath("$.reviews[0].userName").value("Anna Smith"));

    }

    @Test
    @WithUserDetails("jamesbrown")
    void shouldNotAddReviewFromNotCurrentUser() throws Exception {
        String json = objectMapper.writeValueAsString(reviewCreateDto);

        mockMvc.perform(post("/films/1/review/1").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isForbidden())
                .andExpect(content().string("Вы пытаетесь совершить действие от чужого имени"));
    }

    @Test
    @WithUserDetails("annasmith")
    void deleteReview() throws Exception {
        reviewCreateDto.setFilmId(2L);
        String json = objectMapper.writeValueAsString(reviewCreateDto);

        mockMvc.perform(post("/films/2/review/1").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isCreated());

        mockMvc.perform(delete("/films/2/review/1/1"))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/films/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rating").isEmpty());
    }
}
