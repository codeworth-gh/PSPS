# -- invitation

# --- !Ups
create table invitations (
  email varchar(64),
  date TIMESTAMP,
  uuid varchar(64),
  sender varchar(64),

  PRIMARY KEY (email, sender)
);

# --- !Downs
drop table invitations;