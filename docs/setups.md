# Play seed project setups

## Database
 

## Adding users
Add first user by `POST`ing the following json to `$SEREVR/admin/api/users`:

```$json
{
  username: "$username",
  password: "$password",
  email: "$email"
}
```

**NOTE**: This endpoint is available from localhost only.
