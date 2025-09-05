DELETE
FROM film_genre;
DELETE
FROM films;

ALTER SEQUENCE films_id_seq RESTART WITH 1;

INSERT INTO films (name, description, release_date, duration, rating, mpa_id)
VALUES ('Inception', 'A thief who steals corporate secrets through the use of dream-sharing technology.', '2010-07-16',
        148, null, 1),
       ('The Shawshank Redemption',
        'Two imprisoned men bond over a number of years, finding solace and eventual redemption through acts of common decency.',
        '1994-09-23', 142, null, 2),
       ('The Godfather',
        'An organized crime dynastys aging patriarch transfers control of his clandestine empire to his reluctant son.',
        '1972-03-24', 175, null, 3),
       ('The Dark Knight',
        'When the menace known as the Joker emerges from his mysterious past, he wreaks havoc and chaos on the people of Gotham.',
        '2008-07-18', 152, null, 1),
       ('Pulp Fiction',
        'The lives of two mob hitmen, a boxer, a gangster’s wife, and a pair of diner bandits intertwine in four tales of violence',
        '2000-09-13', 140, null, 5);

INSERT INTO film_genre
VALUES (1, 1),
       (3, 1),
       (1, 2),
       (8, 3),
       (2, 3),
       (12, 4),
       (2, 5);