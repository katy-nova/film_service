package com.example.films.dto.review;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ReviewForFilmDto {

    private String text;
    private BigDecimal rating;
    private String userName;
}
