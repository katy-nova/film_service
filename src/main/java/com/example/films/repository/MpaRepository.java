package com.example.films.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.films.model.entity.Mpa;

import java.util.Optional;

public interface MpaRepository extends JpaRepository<Mpa, Integer> {

    Optional<Mpa> findById(int id);
}
