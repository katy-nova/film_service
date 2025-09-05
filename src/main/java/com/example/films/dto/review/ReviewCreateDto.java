package com.example.films.dto.review;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class ReviewCreateDto {
    @Nullable
    @Size(max = 200)
    private String text;

    @DecimalMin("0.0")
    @DecimalMax("10.0")
    @NotNull
    private BigDecimal rating;

    @Positive
    private Long userId;

    @Positive
    private Long filmId;
}
