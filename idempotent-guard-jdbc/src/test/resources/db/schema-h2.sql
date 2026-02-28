CREATE TABLE lock_guard (
    id              VARCHAR(128)  NOT NULL,
    start_time_utc  TIMESTAMP     NOT NULL,
    expire_time_utc TIMESTAMP     NOT NULL,
    hold_by         VARCHAR(255)  NOT NULL,
    hold_hint       VARCHAR(255)  NULL,
    hold_token      VARCHAR(36)   NOT NULL DEFAULT '',
    CONSTRAINT PK_LOCK_GUARD PRIMARY KEY (id)
);
