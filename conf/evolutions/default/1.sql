# -- users

# --- !Ups
create table users (
  id serial PRIMARY KEY,
  username varchar(40) UNIQUE,
  name varchar(120),
  email varchar(64),
  encrypted_password varchar(128)
);

# --- !Downs
drop table users;