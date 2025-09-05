package com.example.films.controller;

import com.example.films.dto.film.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.example.films.dto.GenreDto;
import com.example.films.dto.MpaDto;
import com.example.films.dto.film.*;
import com.example.films.dto.review.ReviewCreateDto;
import com.example.films.dto.review.ReviewDto;
import com.example.films.dto.review.ReviewForFilmDto;
import com.example.films.service.films.FilmDescriptionService;
import com.example.films.service.films.FilmLikeService;
import com.example.films.service.films.FilmReviewService;
import com.example.films.service.films.FilmService;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping(path = "/films")
@AllArgsConstructor
@Validated
@Tag(name = "Фильмы")
public class FilmController {

    private final FilmService filmService;
    private final FilmLikeService filmLikeService;
    private final FilmReviewService filmReviewService;
    private final FilmDescriptionService filmDescriptionService;

    @Operation(summary = "Получение списка всех фильмов с возможностью фильтрации")
    @GetMapping
    public Page<FilmListDto> getFilmsByFilters(@PageableDefault Pageable pageable,
                                               @RequestParam(required = false) String name,
                                               @RequestParam(required = false) List<String> genre,
                                               @RequestParam(required = false) BigDecimal rating,
                                               @RequestParam(required = false) Integer fromYear,
                                               @RequestParam(required = false) Integer toYear,
                                               @RequestParam(required = false) List<String> mpa) {
        return filmService.getFilmsByFilter(pageable, name, genre, rating, fromYear, toYear, mpa);
    }

    @Operation(summary = "Получение информации о фильме")
    @GetMapping(path = "/{id}")
    public FilmDto getFilmById(@PathVariable("id") Long id) {
        return filmService.getFilmById(id);
    }

    @Operation(summary = "Добавление фильма",
            description = "Доступно только для администраторов",
            security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public NewFilmDto createFilm(@Valid @RequestBody FilmCreateDto film) {
        return filmService.createFilm(film);
    }

    @Operation(summary = "Обновление информации о фильме",
            description = "Данное действие доступно только для администраторов",
            security = @SecurityRequirement(name = "bearerAuth"))
    @PutMapping(path = "/{id}")
    public FilmDto updateFilm(@PathVariable Long id, @Valid @RequestBody FilmUpdateDto film) {
        return filmService.updateFilm(id, film);
    }

    @Operation(summary = "Удаление фильма",
            description = "Данное действие доступно только для администраторов",
            security = @SecurityRequirement(name = "bearerAuth"))
    @DeleteMapping(path = "/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteFilm(@PathVariable Long id) {
        filmService.deleteFilmById(id);
    }

    @Operation(summary = "Добавление отзыва",
            description = "Данное действие можно совершить только от своего имени." +
                    " Один пользователь может оставить только один отзыв к фильму",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(path = "/{filmId}/review/{userId}")
    @PreAuthorize("@authenticationService.isCurrentUser(#userId)") // если не указываю как PathVariable, не видит ее тут
    public ReviewDto addReview(@PathVariable Long userId, @PathVariable Long filmId, @Valid @RequestBody ReviewCreateDto review) {
        return filmReviewService.addReview(review, userId, filmId);
    }

    @Operation(summary = "Удаление отзыва",
            description = "Данное действие можно совершить только от своего имени.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @DeleteMapping(path = "/{filmId}/review/{userId}/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("@authenticationService.isCurrentUser(#userId)")
    public void deleteReview(@PathVariable Long id, @PathVariable Long userId, @PathVariable Long filmId) {
        filmReviewService.deleteReview(id, filmId, userId);
    }

    @Operation(summary = "Добавление лайка к фильму",
            description = "Данное действие можно совершить только от своего имени" +
                    " Один пользователь может оставить только один лайк к фильму",
            security = @SecurityRequirement(name = "bearerAuth"))
    @PutMapping(path = "/{filmId}/like/{userId}")
    @PreAuthorize("@authenticationService.isCurrentUser(#userId)")
    public FilmDto addLike(@PathVariable Long userId, @PathVariable Long filmId) {
        return filmLikeService.addLike(filmId, userId);
    }

    @Operation(summary = "Удаление лайка к фильму",
            description = "Данное действие можно совершить только от своего имени.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @DeleteMapping(path = "/{filmId}/like/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("@authenticationService.isCurrentUser(#userId)")
    public void deleteLike(@PathVariable Long userId, @PathVariable Long filmId) {
        filmLikeService.deleteLike(filmId, userId);
    }

    @Operation(summary = "Получение списка популярных фильмов")
    @GetMapping(path = "/popular")
    public Page<FilmListDto> getPopularFilms(@Positive @RequestParam(defaultValue = "10") int count,
                                         @RequestParam(defaultValue = "0") int page) {
        return filmService.getTheMostLikedFilms(count, page);
    }

    @Operation(summary = "Получение отзывов к фильму по его id")
    @GetMapping("/{id}/reviews")
    public Page<ReviewForFilmDto> getReviewsForFilm(@PathVariable Long id, @PageableDefault Pageable pageable) {
        return filmReviewService.getReviewsForFilm(id, pageable);
    }

    @Operation(summary = "Получение жанра по id")
    @GetMapping(path = "/genres/{id}")
    public GenreDto getGenreById(@PathVariable @Positive int id) {
        return filmDescriptionService.getGenreById(id);
    }

    @Operation(summary = "Получение списка всех жанров")
    @GetMapping(path = "/genres")
    public List<GenreDto> getGenres() {
        return filmDescriptionService.getGenres();
    }

    @Operation(summary = "Получение списка всех возрастных рейтингов mpa")
    @GetMapping(path = "/mpa")
    public List<MpaDto> getMpa() {
        return filmDescriptionService.getMpas();
    }

    @Operation(summary = "Получение возрастного рейтинга mpa по id")
    @GetMapping(path = "/mpa/{id}")
    public MpaDto getMpaById(@PathVariable @Positive int id) {
        return filmDescriptionService.getMpaById(id);
    }

}
