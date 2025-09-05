package com.example.films.controller.users;

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

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@Sql(scripts = "/friendship_data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/friendship_cleanup.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public class UserFriendshipIntegrationTest {
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
    @WithUserDetails("jamesbrown")
    void shouldGetFriends() throws Exception {
        mockMvc.perform(get("/users/1/friends"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(3)))
                .andExpect(jsonPath("$.content[0].name").value("John Doe"))
                .andExpect(jsonPath("$.content[1].login").value("jamesbrown"))
                .andExpect(jsonPath("$.content[2].login").value("lindawilliams"))
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.number").value(0))
                .andExpect(jsonPath("$.totalPages").value(1));

        mockMvc.perform(get("/users/1/friends?size=2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].name").value("John Doe"))
                .andExpect(jsonPath("$.content[1].login").value("jamesbrown"))
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.number").value(0))
                .andExpect(jsonPath("$.totalPages").value(2));

        mockMvc.perform(get("/users/1/friends?size=2&page=1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].login").value("lindawilliams"))
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.number").value(1))
                .andExpect(jsonPath("$.totalPages").value(2));
    }

    @Test
    @WithUserDetails("jamesbrown")
    void shouldGetFollowersPage() throws Exception {
        mockMvc.perform(get("/users/5/followers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].name").value("John Doe"))
                .andExpect(jsonPath("$.content[1].login").value("jamesbrown"))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.number").value(0))
                .andExpect(jsonPath("$.totalPages").value(1));
    }

    @Test
    @WithUserDetails("jamesbrown")
    void shouldGetFollowingPage() throws Exception {
        mockMvc.perform(get("/users/3/following"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].login").value("annasmith"))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.number").value(0))
                .andExpect(jsonPath("$.totalPages").value(1));
    }

    @Test
    @WithUserDetails("mariajohnson")
    void shouldGetBlackList() throws Exception {
        mockMvc.perform(get("/users/3/blacklist"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].login").value("jamesbrown"))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.number").value(0))
                .andExpect(jsonPath("$.totalPages").value(1));
    }

    @Test
    @WithUserDetails("mariajohnson")
    void shouldGetCommonFriends() throws Exception {
        mockMvc.perform(get("/users/1/friends/common/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].login").value("jamesbrown"))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.number").value(0))
                .andExpect(jsonPath("$.totalPages").value(1));
    }

    @Test
    @WithUserDetails("mariajohnson")
    void shouldAddFriendship() throws Exception {
        mockMvc.perform(put("/users/3/friends/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userLogin").value("mariajohnson"))
                .andExpect(jsonPath("$.friendLogin").value("lindawilliams"))
                .andExpect(jsonPath("$.status").value("REQUESTED"))
                .andExpect(jsonPath("$.createdAt").isNotEmpty());
    }

    @Test
    @WithUserDetails("annasmith")
    void shouldNotAddFriendshipSecondTime() throws Exception {
        mockMvc.perform(put("/users/1/friends/5"))
                .andExpect(status().isConflict())
                .andExpect(content().string(containsString("Пользователь уже добавлен в друзья")));
    }

    @Test
    @WithUserDetails("lindawilliams")
    void shouldAcceptFriendship() throws Exception {
        mockMvc.perform(put("/users/5/friends/4/accept"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userLogin").value("jamesbrown"))
                .andExpect(jsonPath("$.friendLogin").value("lindawilliams"))
                .andExpect(jsonPath("$.status").value("ACCEPTED"))
                .andExpect(jsonPath("$.createdAt").isNotEmpty())
                .andExpect(jsonPath("$.createdAt").isNotEmpty());
    }

    @Test
    @WithUserDetails("annasmith")
    void shouldAcceptFriendship2() throws Exception {
        // я не знаю, насколько это верно, но при попытке отправить заявку в друзья тому, кто уже отправил заявку тебе
        // сервис автоматически перебростит на принятие заявки
        mockMvc.perform(put("/users/1/friends/3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userLogin").value("mariajohnson"))
                .andExpect(jsonPath("$.friendLogin").value("annasmith"))
                .andExpect(jsonPath("$.status").value("ACCEPTED"))
                .andExpect(jsonPath("$.createdAt").isNotEmpty())
                .andExpect(jsonPath("$.createdAt").isNotEmpty());
    }

    @Test
    @WithUserDetails("johndoe")
    void shouldAddFriendToTheBlacklist() throws Exception {
        // здесь не должна поменяться направленность
        mockMvc.perform(put("/users/2/friends/5/block"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userLogin").value("johndoe"))
                .andExpect(jsonPath("$.friendLogin").value("lindawilliams"))
                .andExpect(jsonPath("$.status").value("BLOCKED"))
                .andExpect(jsonPath("$.createdAt").isNotEmpty())
                .andExpect(jsonPath("$.createdAt").isNotEmpty());

        // здесь должна поменяться направленность
        mockMvc.perform(put("/users/2/friends/3/block"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userLogin").value("johndoe"))
                .andExpect(jsonPath("$.friendLogin").value("mariajohnson"))
                .andExpect(jsonPath("$.status").value("BLOCKED"))
                .andExpect(jsonPath("$.createdAt").isNotEmpty())
                .andExpect(jsonPath("$.createdAt").isNotEmpty());
    }

    @Test
    @WithUserDetails("johndoe")
    void shouldNotAddAdminToTheBlacklist() throws Exception {
        mockMvc.perform(put("/users/2/friends/1/block"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Нельзя заблокировать администратора")));
    }

    @Test
    @WithUserDetails("mariajohnson")
    void shouldAddNotFriendToTheBlacklist() throws Exception {
        mockMvc.perform(put("/users/3/friends/5/block"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userLogin").value("mariajohnson"))
                .andExpect(jsonPath("$.friendLogin").value("lindawilliams"))
                .andExpect(jsonPath("$.status").value("BLOCKED"))
                .andExpect(jsonPath("$.createdAt").isNotEmpty());
    }

    @Test
    @WithUserDetails("johndoe")
    void shouldRemoveFriendship() throws Exception {
        mockMvc.perform(get("/users/2/friends"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(3)));
        mockMvc.perform(get("/users/2/followers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)));

        // не меняется направленность
        mockMvc.perform(delete("/users/2/friends/3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userLogin").value("mariajohnson"))
                .andExpect(jsonPath("$.friendLogin").value("johndoe"))
                .andExpect(jsonPath("$.status").value("REQUESTED"))
                .andExpect(jsonPath("$.createdAt").isNotEmpty())
                .andExpect(jsonPath("$.createdAt").isNotEmpty());

        mockMvc.perform(get("/users/2/friends"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)));
        mockMvc.perform(get("/users/2/followers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)));

        // меняется направленность
        mockMvc.perform(delete("/users/2/friends/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userLogin").value("annasmith"))
                .andExpect(jsonPath("$.friendLogin").value("johndoe"))
                .andExpect(jsonPath("$.status").value("REQUESTED"))
                .andExpect(jsonPath("$.createdAt").isNotEmpty())
                .andExpect(jsonPath("$.createdAt").isNotEmpty());

        mockMvc.perform(get("/users/2/friends"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)));
        mockMvc.perform(get("/users/2/followers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)));
    }

    @Test
    @WithUserDetails("lindawilliams")
    void shouldNotRemoveFriendshipWithIncorrectIds() throws Exception {
        mockMvc.perform(delete("/users/5/friends/3"))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("Связь между пользователями с id: 5, 3 не найдена")));

        mockMvc.perform(delete("/users/5/friends/2"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Вы не можете удалить из друзей пользователя," +
                        " который не является вашим другом")));
    }

    @Test
    @WithUserDetails("mariajohnson")
    void shouldRemoveFromTheBlacklist() throws Exception {
        mockMvc.perform(delete("/users/3/friends/4/block"))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithUserDetails("jamesbrown")
    void shouldNotRemoveFromTheBlacklist() throws Exception {
        mockMvc.perform(delete("/users/4/friends/3/block"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Убрать блокировку может только ее инициатор")));
    }

}
