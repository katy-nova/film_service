package com.example.films.service.films;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.example.films.dto.film.FilmDto;
import com.example.films.dto.mapping.FilmMapping;
import com.example.films.dto.mapping.ReviewMapping;
import com.example.films.dto.review.ReviewCreateDto;
import com.example.films.dto.review.ReviewDto;
import com.example.films.model.entity.Film;
import com.example.films.model.entity.Review;
import com.example.films.model.entity.User;
import com.example.films.repository.FilmRepository;
import com.example.films.repository.ReviewRepository;
import com.example.films.repository.UserRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
@RequiredArgsConstructor
class FilmReviewServiceTest {

    @Mock
    private FilmRepository filmRepository;

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private ReviewMapping reviewMapping;

    @Mock
    private FilmMapping filmMapping;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private FilmReviewService filmReviewService;

    private ReviewCreateDto reviewCreateDto;
    private ReviewDto reviewDto;
    private Film film;
    private FilmDto filmDto;
    private User user;
    private Review review;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setName("user");

        film = new Film();
        film.setId(1L);
        film.setRating(BigDecimal.valueOf(5));

        filmDto = new FilmDto();
        filmDto.setRating(BigDecimal.valueOf(5));

        reviewCreateDto = new ReviewCreateDto();
        reviewCreateDto.setFilmId(1L);
        reviewCreateDto.setUserId(1L);
        reviewCreateDto.setText("New Review");
        reviewCreateDto.setRating(BigDecimal.valueOf(9));

        review = new Review();
        review.setId(1L);
        review.setFilm(film);
        review.setUser(user);
        review.setRating(BigDecimal.valueOf(9));

        reviewDto = new ReviewDto();
        reviewDto.setFilmName(film.getName());
        reviewDto.setUserName(user.getName());
        reviewDto.setRating(BigDecimal.valueOf(9));
    }

    @Test
    void addReview() {
        Review review1 = new Review();
        Review review2 = new Review();

        when(filmRepository.existsById(1L)).thenReturn(true);
        when(userRepository.existsById(1L)).thenReturn(true);
        when(reviewRepository.findAllByFilmId(1L)).thenReturn(List.of(review1, review2, review));
        when(filmRepository.findById(film.getId())).thenReturn(Optional.of(film));
        when(reviewMapping.fromDto(reviewCreateDto)).thenReturn(review);
        when(reviewRepository.save(review)).thenReturn(review);
        when(reviewMapping.toReviewDto(review)).thenReturn(reviewDto);
        when(filmRepository.save(film)).thenReturn(film);

        ReviewDto result = filmReviewService.addReview(reviewCreateDto,
                reviewCreateDto.getUserId(), reviewCreateDto.getFilmId());
        assertNotNull(result);
        assertEquals(reviewDto, result);
        assertEquals(BigDecimal.valueOf(6.3), film.getRating());
        verify(filmRepository, times(1)).existsById(1L);
        verify(userRepository, times(1)).existsById(1L);
        verify(reviewRepository, times(1)).findAllByFilmId(1L);
        verify(filmRepository, times(1)).findById(1L);

    }

    @Test
    void addFirstReview() {
        film.setRating(null);

        when(filmRepository.existsById(1L)).thenReturn(true);
        when(userRepository.existsById(1L)).thenReturn(true);
        when(reviewRepository.findAllByFilmId(1L)).thenReturn(List.of(review));
        when(filmRepository.findById(film.getId())).thenReturn(Optional.of(film));
        when(reviewMapping.fromDto(reviewCreateDto)).thenReturn(review);
        when(reviewRepository.save(review)).thenReturn(review);
        when(reviewMapping.toReviewDto(review)).thenReturn(reviewDto);
        when(filmRepository.save(film)).thenReturn(film);

        ReviewDto result = filmReviewService.addReview(reviewCreateDto,
                reviewCreateDto.getUserId(), reviewCreateDto.getFilmId());
        assertNotNull(result);
        assertEquals(reviewDto, result);
        assertEquals(BigDecimal.valueOf(9), film.getRating());
        verify(filmRepository, times(1)).existsById(1L);
        verify(userRepository, times(1)).existsById(1L);
        verify(reviewRepository, times(1)).findAllByFilmId(1L);
        verify(filmRepository, times(1)).findById(1L);
    }

    @Test
    void shouldNotAddReviewToIncorrectFilm() {
        when(filmRepository.existsById(1L)).thenReturn(false);
        when(userRepository.existsById(1L)).thenReturn(true);

        assertThrows(IllegalStateException.class, () -> filmReviewService.addReview(reviewCreateDto,
                reviewCreateDto.getUserId(), reviewCreateDto.getFilmId()));
    }

    @Test
    void shouldNotAddReviewFromIncorrectUser() {
        when(filmRepository.existsById(1L)).thenReturn(true);
        when(userRepository.existsById(1L)).thenReturn(false);

        assertThrows(IllegalStateException.class, () -> filmReviewService.addReview(reviewCreateDto,
                reviewCreateDto.getUserId(), reviewCreateDto.getFilmId()));
    }

    @Test
    void shouldNotAddSecondReviewFromOneUser() {
        when(reviewRepository.findByUserIdAndFilmId(1L, 1L)).thenReturn(Optional.of(review));

        assertThrows(IllegalStateException.class, () -> filmReviewService.addReview(reviewCreateDto,
                reviewCreateDto.getUserId(), reviewCreateDto.getFilmId()));
    }

    @Test
    void shouldDeleteReview() {
        Review review1 = new Review();
        Review review2 = new Review();

        when(reviewRepository.findAllByFilmId(1L)).thenReturn(List.of(review1, review2, review));
        when(filmRepository.findById(film.getId())).thenReturn(Optional.of(film));
        when(filmRepository.save(film)).thenReturn(film);
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(review));

        filmReviewService.deleteReview(1L, 1L, 1L);
        assertEquals(BigDecimal.valueOf(3.0), film.getRating());
        verify(reviewRepository, times(1)).findAllByFilmId(1L);
        verify(filmRepository, times(1)).findById(1L);
    }

    @Test
    void shouldDeleteFirstReview() {
        when(reviewRepository.findAllByFilmId(1L)).thenReturn(List.of(review));
        when(filmRepository.findById(film.getId())).thenReturn(Optional.of(film));
        when(filmRepository.save(film)).thenReturn(film);
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(review));

        filmReviewService.deleteReview(1L, 1L, 1L);
        assertNull(film.getRating());
        verify(reviewRepository, times(1)).findAllByFilmId(1L);
        verify(filmRepository, times(1)).findById(1L);
    }

}