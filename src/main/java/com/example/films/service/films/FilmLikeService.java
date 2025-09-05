package com.example.films.service.films;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.films.dto.film.FilmDto;
import com.example.films.dto.mapping.FilmMapping;
import com.example.films.exception.NotFoundException;
import com.example.films.model.entity.Film;
import com.example.films.model.entity.User;
import com.example.films.repository.FilmRepository;
import com.example.films.repository.UserRepository;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class FilmLikeService {

    private final UserRepository userRepository;
    private final FilmRepository filmRepository;
    private final FilmMapping filmMapping;

    private User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> {
                    String errorMessage = String.format("Пользователь с id '%s' не найден", id);
                    log.error(errorMessage);
                    return new NotFoundException(errorMessage);
                });
    }

    private Film getFilm(Long id) {
        return filmRepository.findById(id)
                .orElseThrow(() -> {
                    String errorMessage = "Фильм с таким id не найден";
                    log.error(errorMessage);
                    return new NotFoundException(errorMessage);
                });
    }

    public FilmDto addLike(Long filmId, Long userId) {
        User user = findById(userId);
        Film film = getFilm(filmId);
        user.likeFilm(film);
        userRepository.save(user);
        filmRepository.save(film);
        return filmMapping.toDto(film);
    }

    public void deleteLike(Long filmId, Long userId) {
        User user = findById(userId);
        Film film = getFilm(filmId);
        user.unlikeFilm(film);
        userRepository.save(user);
        filmRepository.save(film);
    }
}
