# Play seed project setups

## Database

*assuming PostgreSQL is already installed and running*

* `create role base_app_user with password 'base_app_pass';`
* `create database base_app with template=template0 LC_COLLATE='C';`
* `grant ALL PRIVILEGES on DATABASE base_app TO base_app_user;`
* `ALTER ROLE base_app_user WITH LOGIN;`
* On some configurations, you also need to adjust user authentication
  * Need to edit the permission file. See where it's located by typing: `SHOW hba_file;`
  * from local using password by adding the below line at the top:

    `host   base_app          base_app_user                                  md5`

    (for local unix sockets, use `local` rather than `host`)

  * Now reload the configuration by typing in psql console: `select pg_reload_conf();`

## Adding users
Add first user by `POST`ing the following json to `$SEREVR/bkofc/api/users`:

```$json
{
  username: "$username",
  password: "$password",
  email: "$email"
}
```

**NOTE**: This endpoint is available from localhost only.


## Email Setup

PSPS sends emails in certain events (forgotten password, user invitations etc.). To 
allow this, you must update the `play.mailer` part of the configuration with the
details of the sending email account. 