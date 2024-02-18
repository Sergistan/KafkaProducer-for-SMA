create table if not exists users
(
    id       bigserial primary key,
    name     varchar(255) not null unique,
    password varchar(255) not null,
    email    varchar(255) not null unique,
    role     varchar(256)
);