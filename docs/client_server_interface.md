# Meldingsgrensesnitt
* Bruker JSON på alt
* Standard prefixer på vanlige kommandoer - create, read, update, delete ++

### Brukerrelatert
**Opprett bruker**
*Request - Format*
```json
{
    command: "CREATE_USER",
    payload: {
        name: "string",
        email: "email_string",
        password: "hashed_string",
    }
}
```

*Request - Example*
```json
{
    command: "CREATE_USER",
    payload: {
        name: "Jonas",
        email: "jonas.solsvik@gmail.com",
        password: "6265b22b66502d70d5f004f08238ac3c",
    }
}
```

*Response*
```json
{
    status: "SUCCESS" | "ERROR",
    payload: {
        id: "1"
    }
}
```

**Logg Inn**
*Request - Format*
```json
{
    command: "LOG_IN",
    payload: {
        email: "email_string",
        password: "hashed_string",
    }
}
```

*Request - Example*
```json
{
    command: "CREATE_USER",
    payload: {
        name: "Jonas",
        email: "jonas.solsvik@gmail.com",
        password: "6265b22b66502d70d5f004f08238ac3c",
    }
}
```

*Response*
```json
{
    status: "SUCCESS" | "ERROR",
    payload: {
        id: "1"
    }
}
```
