
create table lock_guard (
    id                   VARCHAR(128)     not null,
    start_time_utc       DATETIME         not null,
    expire_time_utc      DATETIME         not null,
    hold_by              VARCHAR(255)         not null,
    hold_hint            VARCHAR(255)         null,
    constraint PK_LOCK_GUARD primary key (id)
);
