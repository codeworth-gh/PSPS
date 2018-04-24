# -- forgot password

# -- !Ups
create table uuid_for_forgot_password(
  username varchar(64),
  uuid    varchar(64),
  reset_password_date TIMESTAMP,

  PRIMARY KEY (username, uuid)
);

# -- !Downs
drop table uuid_for_forgot_password;