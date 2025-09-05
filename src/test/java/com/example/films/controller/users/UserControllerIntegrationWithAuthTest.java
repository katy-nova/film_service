package com.example.films.controller.users;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithAnonymousUser;
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

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@WithUserDetails("mariajohnson") // пользователь 3 с правами уровня пользователь
class UserControllerIntegrationWithAuthTest {
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
    void shouldReturnAllUsers() throws Exception {
        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(5)))
                .andExpect(jsonPath("$.totalElements").value(5))
                .andExpect(jsonPath("$.number").value(0))
                .andExpect(jsonPath("$.totalPages").value(1));
    }

    @Test
    @WithAnonymousUser
    void shouldNotReturnAllUsersWithoutAuth() throws Exception {
        mockMvc.perform(get("/users"))
                .andExpect(status().isForbidden());
    }

    @Test
    @Sql(statements = "INSERT INTO friendship(user_id, friend_id, status) VALUES (4, 3, 'BLOCKED')")
    void shouldReturnUserById() throws Exception {
        mockMvc.perform(get("/users/1"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/users/4"))
                .andExpect(status().isForbidden())
                .andExpect(content().string(containsString("Пользователь ограничил доступ к своим данным")));
    }

    @Test
    void shouldAddFriendship() throws Exception {
        mockMvc.perform(put("/users/3/friends/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userLogin").value("mariajohnson"))
                .andExpect(jsonPath("$.friendLogin").value("johndoe"))
                .andExpect(jsonPath("$.status").value("REQUESTED"));

        mockMvc.perform(put("/users/2/friends/1"))
                .andExpect(status().isForbidden())
                .andExpect(content().string(containsString("Вы пытаетесь совершить действие от чужого имени")));
    }

    @Test
    void shouldDeleteUser() throws Exception {
        mockMvc.perform(delete("/users/3"))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/users/3"))
                .andExpect(status().isNotFound());

        mockMvc.perform(delete("/users/2"))
                .andExpect(status().isForbidden())
                .andExpect(content().string(containsString("Вы пытаетесь совершить действие от чужого имени" +
                        " или требующие права администратора")));
    }

    @Test
    @WithUserDetails("annasmith")
        // пользователь 1 с правами администратора
    void shouldDeleteWithAdminAuth() throws Exception {
        mockMvc.perform(delete("/users/5"))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldDenyAdminMethods() throws Exception {
        mockMvc.perform(put("/users/admin/make/2"))
                .andExpect(status().isForbidden());

        mockMvc.perform(put("/users/admin/remove/2"))
                .andExpect(status().isForbidden());

        mockMvc.perform(put("/users/admin/enable/2"))
                .andExpect(status().isForbidden());

        mockMvc.perform(put("/users/admin/disable/2"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithUserDetails("annasmith")
    void shouldMakeAndRemoveAdmin() throws Exception {
        mockMvc.perform(put("/users/admin/make/3"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Для пользователя с id: 3 установлены права администратора")));

        mockMvc.perform(put("/users/admin/remove/3"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Для пользователя с id: 3 аннулированы права администратора")));
    }

    @Test
    @WithUserDetails("annasmith")
    void shouldMakeEnabledAndDisabled() throws Exception {
        mockMvc.perform(put("/users/admin/disable/5"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Пользователь с id: 5 деактивирован")));

        mockMvc.perform(put("/users/admin/enable/5"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Пользователь с id: 5 активирован")));
    }
}
