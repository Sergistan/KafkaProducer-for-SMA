create table if not exists friends
(
    user_id   int references users (id),
    friend_id int references users (id),
    primary key (user_id, friend_id)
);