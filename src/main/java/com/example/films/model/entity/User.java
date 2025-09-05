package com.example.films.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;
import com.example.films.model.enums.Role;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@NoArgsConstructor
@Getter
@Setter
@Table(name = "users")
@Entity
@EqualsAndHashCode(of = {"id"})
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @NotEmpty(message = "Это поле обязательно для заполнения")
    @Pattern(regexp = "^\\S*$", message = "Поле не должно содержать пробелы")
    @Column(unique = true, nullable = false)
    private String login;

    private String password;

    @NotEmpty(message = "Это поле обязательно для заполнения")
    @Email(message = "Неверный формат email")
    private String email;

    @Past(message = "Дата рождения должна быть в прошлом")
    private LocalDate birthday;

    private boolean enabled = true;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Review> reviews = new HashSet<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private Set<Friendship> friendships = new HashSet<>();

    @OneToMany(mappedBy = "friend", cascade = CascadeType.ALL)
    private Set<Friendship> friendsOf = new HashSet<>();

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "user_likes",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "film_id")
    )
    @BatchSize(size = 20)
    private Set<Film> likedFilms = new HashSet<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "role")
    @Enumerated(EnumType.STRING)
    private Set<Role> roles;


    public void likeFilm(Film film) {
        this.likedFilms.add(film);
        film.getLikedBy().add(this);
    }

    public void unlikeFilm(Film film) {
        this.likedFilms.remove(film);
        film.getLikedBy().remove(this);
    }

    public void makeAdmin() {
        this.roles.add(Role.ADMIN);
    }

    public void makeUser() {
        this.setRoles(Set.of(Role.USER));
    }
}
