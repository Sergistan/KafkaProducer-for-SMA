create table if not exists messages
(
    id        bigserial primary key,
    sent_at   timestamp,
    text      varchar(255) not null,
    sender_id bigserial    references users (id) on delete set null,
    chat_id   bigserial    references chats (id) on delete set null
);