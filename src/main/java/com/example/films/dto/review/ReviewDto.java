package com.example.films.dto.review;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class ReviewDto {

    private String text;
    private BigDecimal rating;
    private String userName;
    private String filmName;
}
