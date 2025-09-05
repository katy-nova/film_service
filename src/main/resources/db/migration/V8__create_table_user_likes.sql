CREATE TABLE public.user_likes
(
    user_id bigint NOT NULL,
    film_id bigint NOT NULL,
    CONSTRAINT user_likes_pkey PRIMARY KEY (user_id, film_id),
    CONSTRAINT fk6aog39hkl1hs1amxef5i9g4fv FOREIGN KEY (user_id)
        REFERENCES public.users (id)
        ON UPDATE NO ACTION
        ON DELETE CASCADE,
    CONSTRAINT fkqpqh0hx3mdvucat8mt9ov1gwf FOREIGN KEY (film_id)
        REFERENCES public.films (id)
        ON UPDATE NO ACTION
        ON DELETE CASCADE
);