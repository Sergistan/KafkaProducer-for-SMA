create table if not exists friend_requests
(
    user_id   int references users (id),
    friend_request_id int references users (id),
    primary key (user_id, friend_request_id)
);