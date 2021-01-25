# -- users

# --- !Ups
create table users (
  id serial PRIMARY KEY,
  username varchar(64) UNIQUE,
  name varchar(120),
  email varchar(64),
  encrypted_password varchar(128)
);

CREATE UNIQUE INDEX lowercase_user_names ON users (lower(username));
CREATE UNIQUE INDEX lowercase_user_emails ON users (lower(email));

# --- !Downs
drop index lowercase_user_emails;
drop index lowercase_user_names;
drop table users;
