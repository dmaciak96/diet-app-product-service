create table if not exists product
(
    id                uuid primary key unique,
    name              varchar(255)     NOT NULL,
    type              varchar(100),
    kcal              double precision NOT NULL,
    version           int              NOT NULL,
    created_date      TIMESTAMP        NOT NULL,
    last_updated_date TIMESTAMP        NOT NULL
);