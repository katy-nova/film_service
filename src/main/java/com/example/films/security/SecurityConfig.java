package com.example.films.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import com.example.films.security.jwt.JwtFilter;

import static com.example.films.model.enums.Role.ADMIN;
import static com.example.films.model.enums.Role.USER;

@RequiredArgsConstructor
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    private final JwtFilter jwtFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .httpBasic(AbstractHttpConfigurer::disable)
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/users/registration", "/auth/**").permitAll()
                        .requestMatchers(HttpMethod.GET,
                                "/swagger-ui/**",
                                "/swagger-ui/index.html",
                                "/webjars/**",
                                "/swagger-resources/**",
                                "/v3/api-docs/**"
                        ).permitAll()
                        .requestMatchers(HttpMethod.GET,
                                "/films",
                                "/films/popular",
                                "/films/{id}/**",
                                "/films/genres/**",
                                "/films/mpa/**").permitAll()
                        .requestMatchers("/users/admin/**").hasAuthority(ADMIN.getAuthority())
                        .requestMatchers(HttpMethod.DELETE, "/films/{id}").hasAuthority(ADMIN.getAuthority())
                        .requestMatchers(HttpMethod.PUT, "/films/{id}").hasAuthority(ADMIN.getAuthority())
                        .requestMatchers(HttpMethod.POST, "/films").hasAuthority(ADMIN.getAuthority())
                        .requestMatchers("/users/**").hasAnyAuthority(USER.getAuthority(), ADMIN.getAuthority())
                        .requestMatchers("/films/**").hasAnyAuthority(USER.getAuthority(), ADMIN.getAuthority())
                        .requestMatchers("/actuator/**").hasAuthority(ADMIN.getAuthority())
                        .requestMatchers("/**").authenticated())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(4);
    }
}
