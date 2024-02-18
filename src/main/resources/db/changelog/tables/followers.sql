create table if not exists followers
(
    user_id     int references users (id),
    follower_id int references users (id),
    primary key (user_id, follower_id)
);