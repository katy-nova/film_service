package com.example.films.service.films;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.films.dto.GenreDto;
import com.example.films.dto.MpaDto;
import com.example.films.dto.mapping.GenreMapping;
import com.example.films.dto.mapping.MpaMapping;
import com.example.films.exception.NotFoundException;
import com.example.films.repository.GenreRepository;
import com.example.films.repository.MpaRepository;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FilmDescriptionService {

    private final GenreRepository genreRepository;
    private final MpaRepository mpaRepository;
    private final GenreMapping genreMapping;
    private final MpaMapping mpaMapping;

    public GenreDto getGenreById(int genreId) {
        return genreRepository.findById(genreId).map(genreMapping::toDto)
                .orElseThrow(() -> new NotFoundException("Жанр с id " + genreId + "не найден"));
    }

    public List<GenreDto> getGenres() {
        return genreRepository.findAll().stream().map(genreMapping::toDto).toList();
    }

    public MpaDto getMpaById(int mpaId) {
        return mpaRepository.findById(mpaId).map(mpaMapping::toMpaDto)
                .orElseThrow(() -> new NotFoundException("Жанр с id " + mpaId + "не найден"));
    }

    public List<MpaDto> getMpas() {
        return mpaRepository.findAll().stream().map(mpaMapping::toMpaDto).toList();
    }
}
