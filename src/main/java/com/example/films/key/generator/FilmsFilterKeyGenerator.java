package com.example.films.key.generator;

import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Component("filmsFilterKeyGenerator")
public class FilmsFilterKeyGenerator implements KeyGenerator {

    @Override
    public Object generate(Object target, Method method, Object... params) {
        Pageable pageable = (Pageable) params[0];
        String name = (String) params[1];
        List<String> genre = (List<String>) params[2];
        BigDecimal rating = (BigDecimal) params[3];
        Integer fromYear = (Integer) params[4];
        Integer toYear = (Integer) params[5];
        List<String> mpa = (List<String>) params[6];

        StringBuilder key = new StringBuilder();

        key.append("page=").append(pageable.getPageNumber()).append(";");
        key.append("size=").append(pageable.getPageSize()).append(";");

        if (name != null) {
            key.append("name=").append(name).append(";");
        }

        if (genre != null && !genre.isEmpty()) {
            key.append("genre=").append(genre.stream().sorted().collect(Collectors.joining(","))).append(";");
        }

        if (rating != null) {
            key.append("rating=").append(rating).append(";");
        }

        if (fromYear != null) {
            key.append("fromYear=").append(fromYear).append(";");
        }

        if (toYear != null) {
            key.append("toYear=").append(toYear).append(";");
        }

        if (mpa != null && !mpa.isEmpty()) {
            key.append("mpa=").append(mpa.stream().sorted().collect(Collectors.joining(","))).append(";");
        }

        return key.toString();
    }
}

