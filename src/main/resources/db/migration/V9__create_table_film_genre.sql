CREATE TABLE public.film_genre
(
    genre_id integer NOT NULL,
    film_id bigint NOT NULL,
    CONSTRAINT film_genre_pkey PRIMARY KEY (genre_id, film_id),
    CONSTRAINT fk5ak2a33dwsg8k75gwaheplxf2 FOREIGN KEY (film_id)
        REFERENCES public.films (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,
    CONSTRAINT fkd4b34b812xlb3nxh9b9m021dk FOREIGN KEY (genre_id)
        REFERENCES public.genre (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
);

CREATE INDEX IF NOT EXISTS idx_film_genre_film_id ON public.film_genre (film_id);

CREATE INDEX IF NOT EXISTS idx_film_genre_genre_id ON public.film_genre (genre_id);