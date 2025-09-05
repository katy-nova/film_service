package com.example.films.repository;

import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import com.example.films.model.entity.Film;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Hidden
@RepositoryRestResource(collectionResourceRel = "films", path = "films")
public interface FilmRepository extends JpaRepository<Film, Long>, JpaSpecificationExecutor<Film> {

    Optional<Film> findByName(String name);

    Optional<Film> findById(Long id);

    @Query("SELECT f FROM Film f WHERE f.rating >= :minRating")
    List<Film> findFilmsWithRatingGreaterThanOrEqual(@Param("minRating") BigDecimal minRating);

    @Query("SELECT f FROM Film f LEFT JOIN f.likedBy u GROUP BY f.id ORDER BY COUNT(u) DESC")
    Page<Film> findTheMostPopularFilms(Pageable pageable);

}
