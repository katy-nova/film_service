package com.example.films.controller.users;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
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
import com.example.films.dto.user.UserCreateDto;
import com.example.films.dto.user.UserUpdateDto;

import java.text.SimpleDateFormat;
import java.time.LocalDate;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class UserControllerWithRequestBodyIntegrationTest {
    // в этом классе проверяем создание и обновление пользователя с учетом ошибок валидации
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

    private ObjectMapper mapper;
    private UserCreateDto userCreateDto;
    private UserUpdateDto userUpdateDto;
    private String requestBody;

    @BeforeEach
    public void setUp() {
        mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.configure(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        mapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd"));

        userCreateDto = new UserCreateDto();
        userCreateDto.setBirthday(LocalDate.of(2000, 10, 8));
        userCreateDto.setName("test");
        userCreateDto.setEmail("test@test.com");
        userCreateDto.setPassword("password");
        userCreateDto.setLogin("test");

        userUpdateDto = new UserUpdateDto();
    }

    @Test
    void shouldAddUser() throws Exception {
        requestBody = mapper.writeValueAsString(userCreateDto);
        mockMvc.perform(post("/users/registration").contentType(MediaType.APPLICATION_JSON).content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value(userCreateDto.getName()))
                .andExpect(jsonPath("$.email").value(userCreateDto.getEmail()))
                .andExpect(jsonPath("$.login").value(userCreateDto.getLogin()))
                .andExpect(jsonPath("$.birthday").value(userCreateDto.getBirthday().toString()));
    }

    @Test
    void shouldAddUserWithNullName() throws Exception {
        userCreateDto.setName(null);
        requestBody = mapper.writeValueAsString(userCreateDto);
        mockMvc.perform(post("/users/registration").contentType(MediaType.APPLICATION_JSON).content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value(userCreateDto.getLogin()));
    }

    @Test
    void shouldNotAddUserWithIncorrectLogin() throws Exception {
        userCreateDto.setLogin("wro ng");
        requestBody = mapper.writeValueAsString(userCreateDto);
        mockMvc.perform(post("/users/registration").contentType(MediaType.APPLICATION_JSON).content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Поле не должно содержать пробелы")));

        userCreateDto.setLogin("");
        requestBody = mapper.writeValueAsString(userCreateDto);
        mockMvc.perform(post("/users/registration").contentType(MediaType.APPLICATION_JSON).content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Это поле обязательно для заполнения")));

        userCreateDto.setLogin(null);
        requestBody = mapper.writeValueAsString(userCreateDto);
        mockMvc.perform(post("/users/registration").contentType(MediaType.APPLICATION_JSON).content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Это поле обязательно для заполнения")));

        userCreateDto.setLogin("annasmith"); // повторное использование логина
        requestBody = mapper.writeValueAsString(userCreateDto);
        mockMvc.perform(post("/users/registration").contentType(MediaType.APPLICATION_JSON).content(requestBody))
                .andExpect(status().isConflict())
                .andExpect(content().string(containsString("Пользователь с логином 'annasmith' уже существует")));
    }

    @Test
    void shouldNotAddUserWithIncorrectEmail() throws Exception {
        userCreateDto.setEmail("wrong");
        requestBody = mapper.writeValueAsString(userCreateDto);
        mockMvc.perform(post("/users/registration").contentType(MediaType.APPLICATION_JSON).content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Неверный формат email")));

        userCreateDto.setEmail("");
        requestBody = mapper.writeValueAsString(userCreateDto);
        mockMvc.perform(post("/users/registration").contentType(MediaType.APPLICATION_JSON).content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Это поле обязательно для заполнения")));

        userCreateDto.setEmail(null);
        requestBody = mapper.writeValueAsString(userCreateDto);
        mockMvc.perform(post("/users/registration").contentType(MediaType.APPLICATION_JSON).content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Это поле обязательно для заполнения")));

        userCreateDto.setEmail("anna.smith@example.com"); // повторное использование email
        requestBody = mapper.writeValueAsString(userCreateDto);
        mockMvc.perform(post("/users/registration").contentType(MediaType.APPLICATION_JSON).content(requestBody))
                .andExpect(status().isConflict())
                .andExpect(content().string(containsString("Пользователь с email 'anna.smith@example.com' уже существует")));
    }

    @Test
    void shouldNotAddUserWithIncorrectBirthday() throws Exception {
        userCreateDto.setBirthday(LocalDate.of(2200, 10, 8));
        requestBody = mapper.writeValueAsString(userCreateDto);
        mockMvc.perform(post("/users/registration").contentType(MediaType.APPLICATION_JSON).content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Дата рождения должна быть в прошлом")));
    }

    @Test
    void shouldNotAddUserWithIncorrectPassword() throws Exception {
        userCreateDto.setPassword("wro ng");
        requestBody = mapper.writeValueAsString(userCreateDto);
        mockMvc.perform(post("/users/registration").contentType(MediaType.APPLICATION_JSON).content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Пароль не должен содержать пробелы")));

        userCreateDto.setPassword("");
        requestBody = mapper.writeValueAsString(userCreateDto);
        mockMvc.perform(post("/users/registration").contentType(MediaType.APPLICATION_JSON).content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Это поле обязательно для заполнения")));

        userCreateDto.setPassword(null);
        requestBody = mapper.writeValueAsString(userCreateDto);
        mockMvc.perform(post("/users/registration").contentType(MediaType.APPLICATION_JSON).content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Это поле обязательно для заполнения")));
    }

    @Test
    @WithUserDetails("jamesbrown")
    void shouldUpdateUser() throws Exception {
        userUpdateDto.setName("New name");
        userUpdateDto.setPassword("NewPassword");
        userUpdateDto.setEmail("new@email.com");
        requestBody = mapper.writeValueAsString(userUpdateDto);
        mockMvc.perform(put("/users/4").contentType(MediaType.APPLICATION_JSON).content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(userUpdateDto.getName()))
                .andExpect(jsonPath("$.email").value(userUpdateDto.getEmail()));
    }

    @Test
    @WithUserDetails("jamesbrown")
    void shouldNotUpdateUserWithIncorrectPassword() throws Exception {
        userCreateDto.setPassword("wro ng");
        requestBody = mapper.writeValueAsString(userCreateDto);
        mockMvc.perform(put("/users/4").contentType(MediaType.APPLICATION_JSON).content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Пароль не должен содержать пробелы")));
    }

    @Test
    @WithUserDetails("jamesbrown")
    void shouldNotUpdateUserWithIncorrectEmail() throws Exception {
        userUpdateDto.setEmail("wrong");
        requestBody = mapper.writeValueAsString(userUpdateDto);
        mockMvc.perform(put("/users/4").contentType(MediaType.APPLICATION_JSON).content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Неверный формат email")));

        userUpdateDto.setEmail("anna.smith@example.com"); // повторное использование email
        requestBody = mapper.writeValueAsString(userUpdateDto);
        mockMvc.perform(put("/users/4").contentType(MediaType.APPLICATION_JSON).content(requestBody))
                .andExpect(status().isConflict())
                .andExpect(content().string(containsString("Пользователь с email 'anna.smith@example.com' уже существует")));
    }

    @Test
    @WithUserDetails("jamesbrown")
    void shouldNotUpdateUserWithIncorrectLogin() throws Exception {
        userUpdateDto.setLogin("wro ng");
        requestBody = mapper.writeValueAsString(userUpdateDto);
        mockMvc.perform(put("/users/4").contentType(MediaType.APPLICATION_JSON).content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Поле не должно содержать пробелы")));

        userCreateDto.setLogin("annasmith"); // повторное использование логина
        requestBody = mapper.writeValueAsString(userCreateDto);
        mockMvc.perform(put("/users/4").contentType(MediaType.APPLICATION_JSON).content(requestBody))
                .andExpect(status().isConflict())
                .andExpect(content().string(containsString("Пользователь с логином 'annasmith' уже существует")));
    }

    @Test
    @WithUserDetails("jamesbrown")
    void shouldNotUpdateUserWithIncorrectBirthday() throws Exception {
        userUpdateDto.setBirthday(LocalDate.of(2200, 10, 8));
        requestBody = mapper.writeValueAsString(userUpdateDto);
        mockMvc.perform(put("/users/4").contentType(MediaType.APPLICATION_JSON).content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Дата рождения должна быть в прошлом")));
    }
}
