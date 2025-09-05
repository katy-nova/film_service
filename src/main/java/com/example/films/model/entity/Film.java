package com.example.films.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "films")
@NoArgsConstructor
@EqualsAndHashCode(of = {"id"})
public class Film {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotEmpty(message = "Это поле обязательно для заполнения")
    private String name;

    @Size(max = 200)
    private String description;

    @Column(name = "release_date")
    @Past(message = "дата релиза должна быть в прошлом")
    private LocalDate releaseDate;

    @Column(precision = 3, scale = 1)
    @DecimalMin("0.0")
    @DecimalMax("10.0")
    private BigDecimal rating; // при использовании типа Double при запуске программа падает в exception

    @Positive(message = "длительность фильма не может быть отрицательной")
    private Integer duration;

    @OneToMany(mappedBy = "film", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Review> reviews = new HashSet<>();

    @ManyToMany(mappedBy = "likedFilms")
    private Set<User> likedBy = new HashSet<>();

    @ManyToMany(mappedBy = "films")
    private Set<Genre> genres = new HashSet<>();

    @ManyToOne(cascade = {CascadeType.MERGE}, fetch = FetchType.LAZY)
    @JoinColumn(name = "mpa_id")
    private Mpa mpa;

    public void setGenres(Set<Genre> genres) {
        this.genres = genres;
        for (Genre genre : genres) {
            genre.addFilm(this);
        }
    }

}