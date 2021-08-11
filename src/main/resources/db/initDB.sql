CREATE TABLE users (
    user_id int primary key ,
    money decimal default 0.0,
    email varchar(50),
    reg_date timestamp,
    position varchar(40)
);

CREATE TABLE payments (
    payment_id serial primary key,
    money decimal not null ,
    payment_date timestamp,
    user_id int,
    foreign key (user_id) references users(user_id)
);

CREATE TABLE participants(
    participant_id serial primary key,
    bet_money decimal not null default 0,
    bet_time timestamp,
    user_id int,
    auction_id int,
    foreign key (user_id) references users(user_id),
    foreign key (auction_id) references auctions(auction_id)
);

CREATE TABLE auctions (
    auction_id serial primary key ,
    start_date timestamp,
    last_bet timestamp,
    thing_name varchar(100) not null ,
    ended boolean,
    winner_id int
)