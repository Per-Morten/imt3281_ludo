`TODO: Skal vi bytte ut action med: Type?`
# TODOS
* [ ] TODO: Skal vi bytte ut action med: Type?
* [ ] TODO: Skal vi ha PascalCase eller snake_case på feilkoder og action navn?

# Meldingsgrensesnitt


## Index
[TOC]


## Validation rules

* **type** - must be of Enum type
* **codes** - must of ErrorCode type
* **username** - Alphanumeric characteres, no whitespace. `@todo Length?`
* **email** - string containing @, no whitespace. `@todo Length?`
* **password_hash** - md5 hash of password (maybe sha1 or stronger?)
* **id, chatId, ....** - positive integer
* **message** - Human readable string. `@todo Length?`
* **debug** - any string. `@todo Length?`
* **friend_status** - "pending" | "friend". 

## Message definition
The aim of the protocol is to be as consistent as possible. Therefore all messages follow a standard pattern of 'type' and 'payload'. Where the type describes the type of the message and is always interpreted the same, and the payload is an array of objects that are interpreted differently based on the 'type' value.
Messages can also contain a 'token' variable used for authentication if authentication is needed. This is only the case when clients are talking to the server.
Any variables that is not the 'type', 'token' or inside the 'payload' are not considered part of the API, and can be removed without notice.

## Communication
Communication either happens through 'one-way' data transfers, or 'two-way' transactions in the form of a request. 

* JSON-only protocol.
* The payload could be array or object (`@todo` discuss if we should only allow array to simplify)
* Prefix type names with:
    * create, read, update, delete
    * send, receive
    * join, leave


### Request Responses
All requests expects a response, either in the form of a success, or an error. The 'type' of the response will be the 'type' name of the request (where \_request is trimmed) postfixed with either \_success or \_error.

### Success Responses
In the cases where a request succeeds, the data within the payload is interpreted based on the 'type' itself. 
Unless otherwise noted, all responses are guaranteed to have at least one element in their payload.

#### Error Responses
All requests can fail, and when they do they follow a consistent format. 
In the case where a request failed, the payload will contain all the codes for the different errors that occured. Upon reception of an error response at least one error code is guaranteed, but multiple error codes may be supplied.

`@examples`

```json
{    
    "type": "create_user_error",
    "payload": [
        {"code": "invalid_email" },
        {"code": "username_too_long" },
        {"code": "invalid_password" }
    ]
}
```
##### Error Constants
In the case an error occurs you should use the explicit enum values supplied by the Error class. The actual numerical codes for the different error values are ommited to avoid implementers using them rather than the constants.

*invalid_email*
Occurs when the supplied email for a create_user request does not meet the requirements of an email. I.e. It fails the regex for valid emails.

*username_too_long*
Occurs when the supplied username is too long to be put into the database.

*invalid_token*
Ocurs when a token has timed out or otherwise is invalid.

`@todo` How should we respond that the password supplied wasn't correct? Do we just have a generic: Login failed because of username password missmatch?

#### Verify token

## TODO: Explain why this is the generic response
```json
{
    "type": "join_chats_request",
    "payload": [
    ],
    "token": "aegir"
}
{   
    "type": "join_chats_response",
    "payload": [
        {"chat_id": 2},
        {"chat_id": 1},
        {"chat_id": 8},
    ],
    "errors": [
        {"chat_id": 3, "codes": []},
    ],   
}
```

## User API

### create_user
`@brief` - Creates a user with the given username, email and password hash.
`@errors` 

`@examples`

create_user_request
```json
{
    "type": "create_user_request",
    "payload": [
        { 
             "username": "JohnDoe" ,
             "email": "john.doe@ubermail.com",
             "password_hash": "6265b22b66502d70d5f004f08238ac3c"
        }    
    ]
}
```

create_user_success
```json
{
    "type": "create_user_success",
    "payload": [
        { "user_id": 1 }
    ]
}
```

### login

`@Examples`

login_request
```json
{
    "type": "login_request",
    "payload": [
        { 
            "email": "jonas.solsvik@gmail.com", 
            "password_hash": "6265b22b66502d70d5f004f08238ac3c" 
        }
    ]
}
```

login_success
```json
{
    "type": "login_success",
    "payload": [
        { "user_id": 2, "token": "f136803ab9c241079ba0cc1b5d02ee77" }
    ]
}
```

### logout
Note: No elements in the payload is required, but the payload variable is still required.
`@requires` authentication

`@Examples`

logout
```json
{
    "type": "logout",
    "payload": [],
    "token": "f13680.."
}
```

### update_user

`@brief` Tries to update user information. All 
`@requires` authentication
`@examples`

update_user_request
```json
{
    "type": "update_user_request",
    "payload": [
        { 
             "user_id": 2,
             "username": "JohnDoe" ,
             "email": "john.doe@ubermail.com",
             "password_hash": "6265b22b66502d70d5f004f08238ac3c"
        }
    ],
    "token": "f13680.."
}
```
update_user_success




### delete_user

`@brief`
`@requires` authentication

`@examples`
delete_user_request
```json
{
    "type": "delete_user_request",
    "payload": [
        {"user_id": 3 }
    ],
    "token": "f1368.." 
}
```

delete_user_success
```json
{
    "type": "delete_user_success",
    "payload": [
        { "user_id": 2 }
    ]
}
```

### get_users
`@brief` Get array of the requested users.
`@requires` authentication
`@note` All information on the user with "user_id" belonging to the authentication token, minimal information on others. The fields on the users with minimal information are still present but empty.
`@minimal information` Name, Avatar_uri

`@examples`
get_users_request (The token belongs to user_id: 3)
```json
{
    "type": "get_users_request",
    "payload": [
        {"user_id": 3},
        {"user_id": 4},
    ],
    "token": "f1368.." 
}
```

get_users_success
```json
{
    "type": "get_users_success",
    "payload": [
        {
            "user_id": 3, 
            "username": "John Doe", 
            "avatar_uri": "http://imgur.com/myavatar", 
            "email": "john.doe@gmail.com"
        },
        {
            "user_id": 4, 
            "username": "Jenna", 
            "avatar_uri": "http://imgur.com/myavatar", 
            "email": "" 
        },
    ],
    "token": "f1368.." 
}
```

## Friends API
### get_friends

`@brief`
`@requires` authentcation

`@examples`
get_friends_request
```json
{
    "type": "get_friends_request",
    "payload": [
        {"user_id": 3},
    ],
    "token": "f1368.." 
}
```

get_friends_success
```json
{
    "type": "get_friends_success",
    "payload": [
        {
            "user_id": 4, 
            "username": "Jenna", 
            "avatar_uri": "http://imgur.com/myavatar", 
            "email": "",
            "status": "friend"
        },
        {
            "user_id": 5, 
            "username": "Garry", 
            "avatar_uri": "http://imgur.com/myavatar", 
            "email": "",
            "status": "pending"
        }
    ],
}
```


### friend
`@brief`
`@requires` authentication

`@examples`

friend_request
```json
{
    "type": "friend_request",
    "payload": [
        {"user_id": 3, "friend_id": 4},
        {"user_id": 3, "friend_id": 5},
    ],
    "token": "f1368.." 
}
```

friend_success
```json
{
    "type": "friend_success",
    "payload": []
}
```

### unfriend

`@brief`
`@requires` authentication

`@examples`

unfriend_request
```json
{
    "type": "ufriend_request",
    "payload": [
        {"user_id": 3, "friend_id": 4},
        {"user_id": 3, "friend_id": 5}
    ],
    "token": "f1368.." 
}
```

unfriend_success
```json
{
    "type": "ufriend_success",
    "payload": []
}
```

### friend_accepted

`@brief` Server push notification when both friends have friended each other.

friend_accepted
```json
{
    "type": "friend_accepted",
    "payload": [
        {"user_id": 3, "friend_id": 4},
    ]
}
````

## Chat API

### create_chat

`@brief` - Create chat with given name
`@requires` authentication

`@Examples`

create_chat_request
```json
{
    "type": "create_chat_request",
    "payload": [
        {"user_id": 2, "name": "My cool chat"}
    ],
    "token": "f4029..",
}
```

create_chat_response
```json
{
    "type": "create_chat_response",
    "payload": [
        {"chat_id": 4}
    ],
    "errors": []
}
```

### join_chats

`@brief` - join chosen chats
`@requires` authentication

`@examples`

join_chats_request
```json
{
    "type": "join_chats_request",
    "payload": [
        {"user_id": 3, "chat_id": 2},
        {"user_id": 3, "chat_id": 1},
        {"user_id": 3, "chat_id": 3},
        {"user_id": 3, "chat_id": 8}
    ],
    "token": "f4029.."
}
```

join_chats_response
```json
{
    "type": "join_chats_response",
    "payload": [
        {"chat_id": 2},
        {"chat_id": 1},
        {"chat_id": 8}
    ],
    "errors": [
        {"chat_id": 3, "codes": ["access_denied"]}
    ],   
}
```

### get_chats
`@brief` - Gets all the non-game chats
`@requires` - authentication

`@Examples`

get_chats_request
```json
{
    "type": "get_chats_request",
    "payload": [
        {"user_id": 2}
    ],
    "token": "f4029.."
}
```


get_chats_response
```json
{
    "type": "get_chats_response",
    "payload": [
        {"chat_id": 2, "name": "my cool chat" },
        {"chat_id": 3, "name": "my cool chat2" }
    ],
    "errors": []
}
```

### send_chat_message
`@brief` - Send message to all participants of any active chat
`@requires` - authentication

`@example`

```json
{
    "type": "send_chat_message_request",
    "payload": [
        {"user_id": 3, "chat_id": 2, "message": "Hei", "verify_token": 23},
        {"user_id": 3, "chat_id": 2, "message": "på", "verify_token": 24}
    ],
    "token": "f4029.."
}
```

```json
{
    "type": "send_chat_message_response",
    "payload": [
        {"verify_token": 24}
    ],
    "errors": [
        {"verify_token": 23, "codes": ["message_to_long"]}
    ],
}
```

### chat_message
`@brief` - Send message to all participants of any active chat
`@requires` - authentication

`@example`

```json
{
    "type": "chat_message",
    "payload": [
        {"user_id": 3, "chat_id": 2, "message": "Hei"},
        {"user_id": 3, "chat_id": 2, "message": "på"}
    ]
}
```

### send_chat_invite

`@Brief` - Recieve chat invite from any of my friends.
`@Constraint` - `fromId` has to be the id of friend

`@example`

```json
{
    "type": "chat_invite",
    "payload": [
        {"user_id": 2, "invitee": 1, "chat_id": 4, "verify_token": 25},
        {"user_id": 2, "invitee": 2, "chat_id": 3, "verify_token": 26},

    ],
    "token": "f4029.."
}
```

### chat_invite
`@Brief` - Recieve chat invite from any of my friends.
`@Constraint` - `fromId` has to be the id of friend

`@example`

```json
{
    "type": "chat_invite",
    "payload": [
        {"user_id": 2, "invitee": 1, "chat_id": 4},
    ]
}
```

### chat_participants

`@Brief` - Push list of participants to client on every update of participant list.

`@example`

```json
{
    "type": "chat_participants",
    "payload": [
        {
            "chat_id": 2, 
            "participants": [
                {"user_id": 3},
                {"user_id": 4}, 
                {"user_id": 5}
            ]
        }
    ]
}
```



## Game API


### move_piece

### roll_dice

### update


## Protocol (Who sends what messages where)


