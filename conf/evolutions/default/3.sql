# -- forgot password

# -- !Ups
create table password_reset_requests(
  username varchar(64),
  uuid    varchar(64),
  reset_password_date TIMESTAMP,

  PRIMARY KEY (username, uuid)
);

# -- !Downs
drop table password_reset_reqeusts;