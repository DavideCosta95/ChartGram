create table users
(
    id bigserial
        constraint users_pk
            primary key,
    telegram_id varchar not null,
    telegram_first_name varchar,
    telegram_last_name varchar,
    telegram_username varchar,
    inserted_at timestamp not null
);

create unique index users_telegram_id_uindex
    on users (telegram_id);


create table groups
(
    id bigserial
        constraint groups_pk
            primary key,
    telegram_id varchar not null,
    description varchar,
    inserted_at timestamp not null
);

create unique index groups_telegram_id_uindex
    on groups (telegram_id);


create table join_events
(
    id bigserial
        constraint join_events_pk
            primary key,
    joined_at timestamp not null,
    joining_user_id bigint not null
        constraint join_events_users_id_fk
            references users
            on update cascade on delete cascade,
    adder_user_id bigint
        constraint join_events_users_id_fk_2
            references users
            on update cascade on delete cascade
);


create table leave_events
(
    id bigserial
        constraint leave_events_pk
            primary key,
    leaving_at timestamp not null,
    leaving_user_id bigint not null
        constraint leave_events_users_id_fk
            references users
            on update cascade on delete cascade,
    remover_user_id bigint
        constraint leave_events_users_id_fk_2
            references users
            on update cascade on delete cascade
);


create table messages
(
    id bigserial
        constraint messages_pk
            primary key,
    sent_at timestamp not null,
    sender_id bigint not null
        constraint messages_users_id_fk
            references users
            on update cascade on delete cascade,
    group_id bigint not null
        constraint messages_groups_id_fk
            references groups
            on update cascade on delete cascade,
    text varchar,
    has_media bool not null
);
