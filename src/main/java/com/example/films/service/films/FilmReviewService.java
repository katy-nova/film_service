package com.example.films.service.films;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import com.example.films.dto.mapping.ReviewMapping;
import com.example.films.dto.review.ReviewCreateDto;
import com.example.films.dto.review.ReviewDto;
import com.example.films.dto.review.ReviewForFilmDto;
import com.example.films.exception.NotFoundException;
import com.example.films.model.entity.Film;
import com.example.films.model.entity.Review;
import com.example.films.repository.FilmRepository;
import com.example.films.repository.ReviewRepository;
import com.example.films.repository.UserRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

@Service
@AllArgsConstructor
@Slf4j
@Transactional(isolation = Isolation.REPEATABLE_READ)
public class FilmReviewService {

    private final ReviewRepository reviewRepository;
    private final FilmRepository filmRepository;
    private final UserRepository userRepository;
    private final ReviewMapping reviewMapping;

    private void calculateRating(Review review) {
        Long id = review.getFilm().getId();
        BigDecimal rating = review.getRating();
        log.debug("Добавление оценки фильма с ID: {}", id);
        Film film = getFilm(id);
        int numberOfReviews = reviewRepository.findAllByFilmId(id).size();
        BigDecimal currentRating = film.getRating();
        if (currentRating == null) {
            log.debug("У фильма нет других оценок");
            film.setRating(rating);
            log.info("Новый рейтинг фильма: {}, количество оценок: {}", rating, 1);
            filmRepository.save(film);
            return;
        }
        BigDecimal numberOfReviewsBigDecimal = BigDecimal.valueOf(numberOfReviews);
        log.debug("Расчет нового рейтинга");
        BigDecimal newRating = currentRating.multiply(numberOfReviewsBigDecimal.subtract(BigDecimal.ONE))
                .add(rating)
                .divide(numberOfReviewsBigDecimal, 1, RoundingMode.HALF_UP);

        film.setRating(newRating);
        log.info("Новый рейтинг фильма: {}, количество оценок: {}", newRating, numberOfReviews);
        filmRepository.save(film);
    }

    @CacheEvict(cacheNames = "longFilmCache", key = "#filmId")
    public ReviewDto addReview(ReviewCreateDto review, Long userId, Long filmId) {
        log.debug("Попытка добавления отзыва к фильму: {}", review.getFilmId());
        if (!userId.equals(review.getUserId()) || !filmId.equals(review.getFilmId()) ) {
            String errorMessage = "Неверно указаны id пользователя или фильма";
            log.error(errorMessage);
            throw new IllegalStateException(errorMessage);
        }
        Optional<Review> oldReview = reviewRepository.findByUserIdAndFilmId(review.getUserId(), review.getFilmId());
        if (oldReview.isPresent()) {
            String errorMessage = String.format("Пользователь с Id: %d уже оставил отзыв от фильме с Id: %d",
                    review.getUserId(), review.getFilmId());
            log.error(errorMessage);
            throw new IllegalStateException(errorMessage);
        }
        boolean exists_user = userRepository.existsById(review.getUserId());
        boolean exists_film = filmRepository.existsById(review.getFilmId());
        if (!exists_user || !exists_film) {
            String errorMessage = "Неверно указаны id пользователя или фильма";
            log.error(errorMessage);
            throw new IllegalStateException(errorMessage);
        }
        Review fromDto = reviewMapping.fromDto(review);
        reviewRepository.save(fromDto);
        log.info("Отзыв к фильму с id: {} успешно сохранен", review.getFilmId());
        calculateRating(fromDto);
        return reviewMapping.toReviewDto(fromDto);
    }

    @CacheEvict(cacheNames = "longFilmCache", key = "#filmId")
    public void deleteReview(Long id, Long filmId, Long userId) {
        Review review = reviewRepository.findById(id).orElseThrow(() -> new NotFoundException("Отзыв не найден"));
        if (!userId.equals(review.getUser().getId()) || !filmId.equals(review.getFilm().getId()) ) {
            String errorMessage = "Неверно указаны id пользователя или фильма";
            log.error(errorMessage);
            throw new IllegalStateException(errorMessage);
        }
        BigDecimal rating = review.getRating();
        log.debug("Удаление оценки фильма с ID: {}", filmId);
        Film film = getFilm(filmId);
        int numberOfReviews = reviewRepository.findAllByFilmId(filmId).size();
        BigDecimal currentRating = film.getRating();
        if (numberOfReviews == 1) {
            log.debug("У фильма больше нет оценок");
            film.setRating(null);
            reviewRepository.deleteById(id);
            filmRepository.save(film);
            return;
        }
        BigDecimal numberOfReviewsBigDecimal = BigDecimal.valueOf(numberOfReviews);
        log.debug("Расчет нового рейтинга");
        BigDecimal newRating = currentRating.multiply(numberOfReviewsBigDecimal)
                .subtract(rating)
                .divide(numberOfReviewsBigDecimal.subtract(BigDecimal.ONE), 1, RoundingMode.HALF_UP);

        film.setRating(newRating);
        log.info("Новый рейтинг фильма: {}, количество оценок: {}", newRating, numberOfReviews - 1);
        reviewRepository.deleteById(id);
        filmRepository.save(film);
    }

    public Page<ReviewForFilmDto> getReviewsForFilm(Long filmId, Pageable pageable) {
        return reviewRepository.findAllByFilmId(filmId, pageable).map(reviewMapping::toReviewForFilmDto);
    }

    private Film getFilm(Long id) {
        return filmRepository.findById(id)
                .orElseThrow(() -> {
                    String errorMessage = "Фильм с таким id не найден";
                    log.error(errorMessage);
                    return new NotFoundException(errorMessage);
                });
    }

}
