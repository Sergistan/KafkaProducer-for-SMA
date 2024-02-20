create table if not exists posts
(
    id          bigserial primary key,
    description varchar(255) not null,
    message     varchar(255) not null,
    image_link  varchar(512) unique,
    image_name  varchar(255) unique,
    created_at  timestamp,
    user_id     bigserial    references users (id) on delete set null
);