create table if not exists custom_property(
    id uuid primary key unique,
    name varchar(255) not null,
    value varchar(255) not null,
    product_id uuid not null,
    foreign key (product_id) references product(id)
)