package com.example.films.repository;

import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.example.films.model.entity.Review;

import java.util.List;
import java.util.Optional;

@Hidden
public interface ReviewRepository extends JpaRepository<Review, Long> {

    Optional<Review> findByUserIdAndFilmId(Long userId, Long filmId);

    @Modifying
    @Query("DELETE FROM Review r WHERE r.user.id = :userId")
    void deleteAllByUserId(@Param("userId") Long userId);

    @Modifying
    @Query("DELETE FROM Review r WHERE r.film.id = :filmId")
    void deleteAllByFilmId(@Param("filmId") Long filmId);

    List<Review> findAllByFilmId(Long filmId);

    Page<Review> findAllByFilmId(Long filmId, Pageable pageable);

}
