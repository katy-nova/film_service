package com.example.films.key.generator;

import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Component("usersPageKeyGenerator")
public class UsersPageKeyGenerator implements KeyGenerator {

    @Override
    public Object generate(Object target, Method method, Object... params) {
        Long id = (Long) params[0];
        Pageable pageable = (Pageable) params[1];

        StringBuilder key = new StringBuilder();
        key.append(id).append(";");
        key.append(method.getName()).append(";");
        key.append(pageable.getPageNumber()).append(";");
        key.append(pageable.getPageSize()).append(";");
        return key.toString();
    }
}
