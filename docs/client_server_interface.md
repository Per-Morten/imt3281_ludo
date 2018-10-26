# Meldingsgrensesnitt


## Index
[TOC]


## Validation rules

* **action** - must be of type Enum action
* **username** - Alphanumeric characteres, no whitespace. `@todo Length?`
* **email** - string containing @, no whitespace. `@todo Length?`
* **password_hash** - md5 hash of password (maybe sha1 or stronger?)
* **all id** - positive integer
* **message** - Human readable string. `@todo Length?`
* **debug** - any string. `@todo Length?`

## Protocol

* JSON-only protocol.
* The payload could be array or object (`@todo` discuss if we should only allow array to simplify)
* Prefix action names with:
    * create, read, update, delete
    * send, receive
    * join, leave


### Request - Server <- Client

`@brief` - Created on client. Expects a response.

```
{
    action
    payload: [] | {}
    token
}
```

### Response Success - Server -> Client

`@brief` - Created on server. Responding to a request. 


```
{    
    action
    payload: [] | {}
}
```

### Response Error - Server -> Client

`@brief` - Created on server. Responding to a failed request. No payload. Human readable message. Display debug info if development mode.

```
{    
    action
    message
    debug
}
```


### Send - Server <- Client

`@brief` - Client pushing a one-way data stream to the server. Does not want any response.


```
{
    action
    payload: [] | {}
    token
}
```


### Receive Server -> Client

`@brief` - Server pushing one-way data stream to client.

```
{
    action
    payload: []
}
```



## User API

### create_user

`@Examples`

create_user
```json
{
    "action": "create_user",
    "payload": [
        { "username": "JohnDoe" },
        { "email": "john.doe@ubermail.com" },
        { "password_hash": "6265b22b66502d70d5f004f08238ac3c" }
    ],
    "token": ""
}
```

create_user_success
```json
{
    "action": "create_user_success",
    "payload": [
        { "id": "1" }
    ]
}
```
create_user_error
```json
{    
    "action": "create_user_error",
    "message": "User with given username already exists",
    "debug": "JohnDoe == JohnDoe"
}
```


### login

`@Examples`

login
```json
{
    "action": "login",
    "payload": [
        { "email": "jonas.solsvik@gmail.com" },
        { "password": "6265b22b66502d70d5f004f08238ac3c" }
    ],
    "token": ""
}
```

login_success
```json
{
    "action": "login_success",
    "payload": [
        { "token": "f136803ab9c241079ba0cc1b5d02ee77" }
    ]
}
```


login_error
```json
{
    "action": "login_error",
    "message": "Wrong username or password",
    "debug": "Password had 1 wrong character"
}
```

### logout

`@requires` authentication

`@Examples`

logout
``` 
{
    "action": "logout",
    "payload": [],
    "token": "f13680.."
}
```


logout_success
``` 
{
    "action": "logout_success",
    "payload": []
}
```

logout_error
```json
{
    "action": "logout_error",
    "message": "Denied",
    "debug": "Token invalid"
}
```

### update_user

`@brief`
`@requires` authentication
`@examples`

update_user
update_user_success
update_user_error



### delete_user

`@brief`
`@requires` authentication

`@examples`
delete_user
delete_user_success
delete_user_error


## Friends API

### get_friends

`@brief`
`@requires` authentcation

`@examples`
get_friends
get_friends_success
get_friends_error



### add_friend

`@brief`
`@requires` authentication

`@examples`
add_friend
add_friend_success
add_friend_error


### add_friend_response ->

`@brief`
`@example`
add_friend_response

### delete_friend

`@brief`
`@requires` authentication

`@examples`
delete_friend
delete_friend_success
delete_friend_error



## Chat API

### create_chat

`@brief` - Get all chats to choose from
`@requires` authentication

`@Examples`

create_chat
```json
{
    "action": "create_chat",
    "payload": [
        "name": "My cool chat"
    ],
    "token": "f4029..",
}
```

create_chat_success
```json
{
    "action": "create_chat_success",
    "payload": {
        "chatId": 4
    },
}
```

create_chat_error
```json
{
    "action": "create_chat_error",
    "message": "You subscription only allows 4 chats",
    "debug": ""
}
```


### join_chat

`@brief` - join chosen chat
`@requires` authentication

`@examples`

join_chat
```json
{
    "action": "join_chat",
    "payload": [
        "chatId": 2,
    ],
    "token": "f4029.."
}
```

join_chat_success
```json
{
    "action": "join_chat_success",
    "payload": {
        "chatId": 2
    }
}
```

get_active_chats_error
```json
{
    "action": "join_chat_error",
    "message": "Chat is full",
    "debug": "42 of 42 slots occupied in chatid 2"
}
```


### get_active_chats

`@brief` - Get active chats which user is joined

`@examples`

get_active_chats
```json
{
    "action": "get_active_chats",
    "payload": [],
    "token": "f4029.."
}
```

get_active_chats_success
```json
{
    "action": "get_active_chats_success",
    "payload": [
        {"id": 2, "name": "my cool chat" }
    ]
}
```

get_active_chats_error
```json
{
    "action": "get_active_chats_error",
    "message": "Unexpected error occured",
    "debug": "sql uri mongodb://localhost:4443"
}
```

### get_all_chats

`@brief` - Get all chats to choose from

`@Examples`

get_all_chats
```json
{
    "action": "get_all_chats",
    "payload": [],
    "token": "f4029.."
}
```


get_all_chats_success
```json
{
    "action": "get_all_chats_success",
    "payload": [
        {"id": 2, "name": "my cool chat" },
        {"id": 3, "name": "my cool chat2" }
    ]
}
```

get_all_chats_error
```json
{
    "action": "get_all_chats_error",
    "message": "Unexpected error occured",
    "debug": "sql uri mongodb://localhost:4443"
}
```

### send_message <-

`@brief` - Send message to all participants of any active chat
`@requires` - authentication
`@constraint` - `chatId` has to be id of active (joined) chat.

`@example`

```json
{
    "action": "send_message",
    "payload": {
        "chatId": 2,
        "message": "Hei"
    },
    "token": "f4029.."
}
```

### receive_message ->

`@brief` - Receive message from any participants in any active chat.

`@example`

```json
{
    "action": "receive_message",
    "payload": {
        "chatId": 2,
        "message": "Hei"
    }
}
```


### send_chat_invite <-

`@brief` - Send chat invite to friend
`@requires` - authentication
`@constraint` - `toId` has to be id of friend
`@constraint` - `chatId` has to be id of active (joined) chat.

`@example`

```json
{
    "action": "send_chat_invite",
    "payload": {
        "fromId": 2,
        "toId": 3,
        "chatId": 4
    },
    "token": "f4029.."
}
```

### recieve_chat_invite ->

`@Brief` - Recieve chat invite from any of my friends.
`@Direction` - server -> client
`@Constraint` - `fromId` has to be the id of friend

`@example`

```json
{
    "action": "recieve_chat_invite",
    "payload": {
        "fromId": 2,
        "chatId": 4
    }
}
```

### receive_chat_participants ->

`@Brief` - Push list of participants to client on every update of participant list.
`@Direction` - server -> client

`@example`

```json
{
    "action": "receive_chat_participants",
    "payload": {
        "chatId": 2,
        "participants": ["John","Jenna","Fredrik"]
    }
}
```