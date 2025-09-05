package com.example.films.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        SimpleCacheManager cacheManager = new SimpleCacheManager();

        CaffeineCache cache1 = new CaffeineCache("shortFilmCache",
                Caffeine.newBuilder()
                        .expireAfterWrite(5, TimeUnit.MINUTES)
                        .maximumSize(1000)
                        .build());

        CaffeineCache cache2 = new CaffeineCache("longFilmCache",
                Caffeine.newBuilder()
                        .expireAfterWrite(60, TimeUnit.MINUTES)
                        .maximumSize(1000)
                        .build());

        CaffeineCache cache3 = new CaffeineCache("shortUserCache",
                Caffeine.newBuilder()
                        .expireAfterWrite(5, TimeUnit.MINUTES)
                        .maximumSize(1000)
                        .build());

        CaffeineCache cache4 = new CaffeineCache("longUserCache",
                Caffeine.newBuilder()
                        .expireAfterWrite(60, TimeUnit.MINUTES)
                        .maximumSize(1000)
                        .build());

        cacheManager.setCaches(Arrays.asList(cache1, cache2, cache3, cache4));
        return cacheManager;
    }
}
