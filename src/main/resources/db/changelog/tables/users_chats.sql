create table if not exists users_chats
(
    chats_id int references chats (id),
    user_id  int references users (id),
    primary key (chats_id, user_id)
);