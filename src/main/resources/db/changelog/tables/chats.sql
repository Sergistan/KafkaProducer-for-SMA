create table if not exists chats
(
    id           bigserial primary key,
    created_at   timestamp,
    last_message varchar(255) not null
);