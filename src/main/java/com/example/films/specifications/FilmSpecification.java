package com.example.films.specifications;

import org.springframework.data.jpa.domain.Specification;
import com.example.films.model.entity.Film;
import jakarta.persistence.criteria.Expression;

import java.math.BigDecimal;
import java.util.List;

public class FilmSpecification {

    public static Specification<Film> hasTitleLike(String name) {
        return (root, query, criteriaBuilder) -> name == null ? null :
                criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), "%" + name.toLowerCase() + "%");
    }

    public static Specification<Film> hasMpaRating(List<String> mpa) {
        return (root, query, criteriaBuilder)
                -> root.join("mpa").get("name").in(mpa);
    }

    public static Specification<Film> hasGenres(List<String> genres) {
        return (root, query, criteriaBuilder) -> root.join("genres").get("name").in(genres);
    }

    public static Specification<Film> hasReleaseYear(Integer year) {
        return (root, query, criteriaBuilder) -> year == null ? null :
                criteriaBuilder.equal(
                        criteriaBuilder.function(
                                "date_part",
                                Double.class,
                                criteriaBuilder.literal("year"),
                                root.get("releaseDate")),
                        year.doubleValue()
                );
    }

    public static Specification<Film> releaseYearBetween(Integer fromYear, Integer toYear) {
        return (root, query, criteriaBuilder) -> {
            Expression<Double> yearExpr = criteriaBuilder.function(
                            "date_part",
                            Double.class,
                            criteriaBuilder.literal("year"),
                            root.get("releaseDate")
                    );

            if (fromYear != null && toYear != null) {
                return criteriaBuilder.between(yearExpr, fromYear.doubleValue(), toYear.doubleValue());
            } else if (fromYear != null) {
                return criteriaBuilder.greaterThanOrEqualTo(yearExpr, fromYear.doubleValue());
            } else {
                return criteriaBuilder.lessThanOrEqualTo(yearExpr, toYear.doubleValue());
            }
        };
    }

    public static Specification<Film> hasRatingGreaterThanOrEqual(BigDecimal rating) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.greaterThanOrEqualTo(root.get("rating"), rating);
    }

}
