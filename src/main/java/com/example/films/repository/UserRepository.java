package com.example.films.repository;

import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.data.jpa.repository.JpaRepository;
import com.example.films.model.entity.User;

import java.util.Optional;

@Hidden
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findById(Long id);

    Optional<User> findByLogin(String login);
}
