package com.example.films.dto.mapping;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;
import com.example.films.dto.review.ReviewCreateDto;
import com.example.films.dto.review.ReviewDto;
import com.example.films.dto.review.ReviewForFilmDto;
import com.example.films.model.entity.Review;
import com.example.films.repository.FilmRepository;
import com.example.films.repository.UserRepository;

@Mapper(componentModel = "spring")
public abstract class ReviewMapping {
    // пришлось сделать абстрактным классом чтобы заинжектить репозитории

    @Autowired
    UserRepository userRepository;

    @Autowired
    FilmRepository filmRepository;


    public abstract ReviewCreateDto toDto(Review review);

    @Mapping(target = "user", expression = "java(userRepository.findById(dto.getUserId()).orElse(null))")
    @Mapping(target = "film", expression = "java(filmRepository.findById(dto.getFilmId()).orElse(null))")
    public abstract Review fromDto(ReviewCreateDto dto);

    @Mapping(target = "userName", expression = "java(review.getUser().getName())")
    @Mapping(target = "filmName", expression = "java(review.getFilm().getName())")
    public abstract ReviewDto toReviewDto(Review review);

    @Mapping(target = "userName", expression = "java(review.getUser().getName())")
    public abstract ReviewForFilmDto toReviewForFilmDto(Review review);
}
