package com.example.films.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.films.model.entity.Genre;

import java.util.Optional;

public interface GenreRepository extends JpaRepository<Genre, Integer> {

    Optional<Genre> findById(int id);
}
