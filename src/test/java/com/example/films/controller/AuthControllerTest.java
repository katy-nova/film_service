package com.example.films.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import com.example.films.dto.jwt.JwtAuthenticationDto;
import com.example.films.dto.jwt.RefreshTokenDto;
import com.example.films.dto.jwt.UserCredentialDto;
import com.example.films.security.jwt.JwtService;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AuthControllerTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:13")
            .withDatabaseName("filmorate_test")
            .withUsername("test")
            .withPassword("test");
    @Autowired
    private JwtService jwtService;

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

    @Test
    void signIn() throws Exception {
        UserCredentialDto userCredentialDto = new UserCredentialDto();
        userCredentialDto.setLogin("jamesbrown");
        userCredentialDto.setPassword("1234");

        String json = objectMapper.writeValueAsString(userCredentialDto);
        String token = mockMvc.perform(post("/auth/sign-in").contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        JwtAuthenticationDto jwtAuthenticationDto = objectMapper.readValue(token, JwtAuthenticationDto.class);
        assertNotNull(jwtAuthenticationDto);
        assertEquals(userCredentialDto.getLogin(), jwtService.getLoginFromToken(jwtAuthenticationDto.getToken()));

    }

    @Test
    void shouldNotSignIn() throws Exception {
        UserCredentialDto userCredentialDto = new UserCredentialDto();
        userCredentialDto.setLogin("jamesbrown");
        userCredentialDto.setPassword("incorrectpassword");
        String json = objectMapper.writeValueAsString(userCredentialDto);

        mockMvc.perform(post("/auth/sign-in").contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string(containsString("Неверные логин или пароль")));

    }

    @Test
    void refresh() throws Exception {
        UserCredentialDto userCredentialDto = new UserCredentialDto();
        userCredentialDto.setLogin("jamesbrown");
        userCredentialDto.setPassword("1234");

        String json = objectMapper.writeValueAsString(userCredentialDto);
        String token = mockMvc.perform(post("/auth/sign-in").contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        RefreshTokenDto refreshTokenDto = new RefreshTokenDto();
        refreshTokenDto.setRefreshToken(objectMapper.readValue(token, JwtAuthenticationDto.class).getRefreshToken());
        String refreshTokenJson = objectMapper.writeValueAsString(refreshTokenDto);
        mockMvc.perform(post("/auth/refresh").contentType(MediaType.APPLICATION_JSON)
                        .content(refreshTokenJson))
                .andExpect(status().isOk());
    }
}