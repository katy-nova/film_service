DELETE
FROM film_genre;
DELETE
FROM films;

ALTER SEQUENCE films_id_seq RESTART WITH 1;

INSERT INTO films (name, description, release_date, duration, rating, mpa_id)
VALUES ('Comedy2000_1', 'desc', '2000-08-09', 130, 8, 1),
       ('Comedy2005_2', 'desc', '2005-08-10', 130, 7, 2),
       ('ComedyAction2000_1', 'desc', '2000-04-12', 130, 9, 1),
       ('Action2000_2', 'desc', '2000-08-09', 130, 6, 2),
       ('Action2003_3', 'desc', '2003-08-09', 130, 7, 3);
INSERT INTO film_genre (genre_id, film_id)
VALUES (1, 1),
       (1, 2),
       (1, 3),
       (3, 3),
       (3, 4),
       (3, 5);