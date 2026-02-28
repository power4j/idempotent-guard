
create table lock_guard (
    id                   VARCHAR(128)         not null,
    start_time_utc       TIMESTAMP            not null,
    expire_time_utc      TIMESTAMP            not null,
    hold_by              VARCHAR(255)         not null,
    hold_hint            VARCHAR(255)         null,
    hold_token           VARCHAR(36)          not null default '',
    constraint PK_LOCK_GUARD primary key (id)
);
