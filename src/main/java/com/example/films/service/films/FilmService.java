package com.example.films.service.films;

import com.example.films.dto.film.*;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import com.example.films.dto.film.*;
import com.example.films.dto.mapping.FilmMapping;
import com.example.films.dto.mapping.GenreMapping;
import com.example.films.dto.mapping.MpaMapping;
import com.example.films.exception.NotFoundException;
import com.example.films.model.entity.Film;
import com.example.films.repository.FilmRepository;
import com.example.films.repository.ReviewRepository;
import com.example.films.specifications.FilmSpecification;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class FilmService {

    private final FilmRepository filmRepository;
    private final ReviewRepository reviewRepository;
    private final FilmMapping filmMapping;
    private final GenreMapping genreMapping;
    private final MpaMapping mpaMapping;

    @Cacheable(cacheNames = "longFilmCache", key = "#id")
    public FilmDto getFilmById(Long id) {
        return filmMapping.toDto(getFilm(id));
    }

    private Film getFilm(Long id) {
        return filmRepository.findById(id)
                .orElseThrow(() -> {
                    String errorMessage = "Фильм с таким id не найден";
                    log.error(errorMessage);
                    return new NotFoundException(errorMessage);
                });
    }

    @CacheEvict(cacheNames = "shortFilmCache", allEntries = true)
    @CachePut(cacheNames = "longFilmCache", key = "#result.id")
    @Transactional
    public NewFilmDto createFilm(FilmCreateDto film) {
        Film fromDTO = filmMapping.fromCreateDto(film);
        filmRepository.save(fromDTO);
        log.info("Фильм сохранен");
        return filmMapping.toNewFilmDto(fromDTO);
    }

    @CacheEvict(cacheNames = "shortFilmCache", allEntries = true)
    @CachePut(cacheNames = "longFilmCache", key = "#id")
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public FilmDto updateFilm(Long id, FilmUpdateDto film) {
        log.debug("Попытка обновить данные фильма с ID: {}", id);
        Film oldFilm = getFilm(id);
        if (film.getName() != null && !film.getName().equals(oldFilm.getName())) {
            oldFilm.setName(film.getName());
            log.info("Название фильма: {} успешно обновлено", oldFilm.getName());
        }
        if (film.getDescription() != null && !film.getDescription().equals(oldFilm.getDescription())) {
            oldFilm.setDescription(film.getDescription());
            log.info("Описание фильма: {} успешно обновлено", oldFilm.getDescription());
        }
        if (film.getGenres() != null) {
            oldFilm.setGenres(film.getGenres().stream().map(genreMapping::fromDto).collect(Collectors.toSet()));
            log.info("Жанр фильма: {} успешно обновлен", oldFilm.getGenres());
        }
        if (film.getDuration() != null && film.getDuration() != 0 && !(film.getDuration().equals(oldFilm.getDuration()))) {
            oldFilm.setDuration(film.getDuration());
            log.info("Продолжительность фильма: {} успешно обновлена", oldFilm.getDuration());
        }
        if (film.getReleaseDate() != null && !film.getReleaseDate().equals(oldFilm.getReleaseDate())) {
            oldFilm.setReleaseDate(film.getReleaseDate());
            log.info("Дата релиза: {} успешно обновлена", oldFilm.getReleaseDate());
        }
        if (film.getMpa() != null && film.getMpa().getId() != (oldFilm.getMpa().getId())) {
            oldFilm.setMpa(mpaMapping.toMPA(film.getMpa()));
            log.info("MPA рейтинг: {} успешно обновлен", oldFilm.getMpa().getName());
        }
        // не хочется, чтобы количество оценок и рейтинг можно было менять вручную
        filmRepository.save(oldFilm);
        log.info("фильм успешно сохранен");
        return filmMapping.toDto(oldFilm);
    }

    @Caching(evict = {@CacheEvict(cacheNames = "shortFilmCache", allEntries = true),
    @CacheEvict(cacheNames = "longFilmCache", key = "#id")})
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public void deleteFilmById(Long id) {
        // проверяем, что фильм есть в базе
        if (!filmRepository.existsById(id)) {
            throw new NotFoundException("Фильм с id " + id + "не найден");
        }
        log.debug("Попытка удаления всех отзывов к фильму с ID: {}", id);
        reviewRepository.deleteAllByFilmId(id);
        log.info("Отзывы успешно удалены");
        filmRepository.deleteById(id);
        log.info("Фильм с ID: {} успешно удален", id);
    }

    @Cacheable(cacheNames = "shortFilmCache", key = "'popularFilms:' + #count + '-' + #page")
    public Page<FilmListDto> getTheMostLikedFilms(int count, int page) {
        Pageable pageable = PageRequest.of(page, count);
        return filmRepository.findTheMostPopularFilms(pageable).map(filmMapping::toFilmListDto);
    }

    @Cacheable(cacheNames = "shortFilmCache", keyGenerator = "filmsFilterKeyGenerator")
    public Page<FilmListDto> getFilmsByFilter(Pageable pageable, String name, List<String> genre, BigDecimal rating, Integer fromYear,
                                          Integer toYear, List<String> mpa) {
        Specification<Film> spec = Specification.where(null);
        if (name != null) {
            spec = spec.and(FilmSpecification.hasTitleLike(name));
        }
        if (rating != null) {
            spec = spec.and(FilmSpecification.hasRatingGreaterThanOrEqual(rating));
        }
        if (fromYear != null || toYear != null) {
            if (Objects.equals(fromYear, toYear)) { // используем этот метод, чтобы избежать исключений
                spec = spec.and(FilmSpecification.hasReleaseYear(fromYear));
            } else if (toYear != null && fromYear != null && fromYear > toYear) {
                throw new IllegalStateException("Неверно заданы года");
            } else {
                spec = spec.and(FilmSpecification.releaseYearBetween(fromYear, toYear));
            }
        }
        if (genre != null && !genre.isEmpty()) {
            spec = spec.and(FilmSpecification.hasGenres(genre));
        }
        if (mpa != null && !mpa.isEmpty()) {
            spec = spec.and(FilmSpecification.hasMpaRating(mpa));
        }
        return filmRepository.findAll(spec, pageable).map(filmMapping::toFilmListDto);
    }

}
