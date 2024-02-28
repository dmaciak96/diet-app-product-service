create table if not exists product(
  id uuid primary key unique,
  name varchar(255) NOT NULL,
  type varchar(100),
  kcal double precision NOT NULL
);