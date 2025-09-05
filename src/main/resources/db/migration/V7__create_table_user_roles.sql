CREATE TABLE public.user_roles
(
    user_id bigint NOT NULL,
    role character varying(255),
    CONSTRAINT user_roles_user_id_fkey FOREIGN KEY (user_id)
        REFERENCES public.users (id)
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
);