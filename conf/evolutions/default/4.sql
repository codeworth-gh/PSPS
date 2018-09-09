-- Add the setting store

# -- !Ups
create table settings(
  id varchar(32) PRIMARY KEY,
  value text
);

# -- !Downs
drop table settings;