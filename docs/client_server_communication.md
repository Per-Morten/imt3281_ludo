# Server-Client Communication


# Index

[TOC]


# TODOS

# Message protocol

The aim of the protocol is to be as consistent as possible. Therefore all messages follow a standard pattern of 'type' and 'payload'. Where the type describes the type of the message and is always interpreted the same, and the payload is an array of objects that are interpreted differently based on the 'type' value.
Messages can also contain a 'token' variable used for authentication if authentication is needed. This is only the case when clients are talking to the server.
Any variables that is not the 'type', 'token' or inside the 'payload' are not considered part of the API, and can be removed without notice.

* Communication either happens through 'one-way' data transfers, or 'two-way' transactions in the form of a request-response pair.
* Data is structured as valid JSON.
* Three different kinds of messages:
    * Request
    * Response
    * Event

## Request - server <-- client

**Format**

`id` Indentifies a unique request-response-pair.
`type` What type of request this is. Always postfixed with `_request`.
`payload` An array of items containing application specific data
`payload[].id` indentifies a unique item within the payload
`auth_token` an optional parameter. Used if the request requires authentication.

```json
{
    "id": <id>,
    "type": <type>_request,
    "payload": [
        {"id": <id>, ...<data> },
        {"id": <id>, ...<data> }
        ...
    ],
    "auth_token": <token>
}
```

**Example**
```json
{
    "id": 0,
    "type": "create_user_request",
    "payload": [
        {
             "id": 0,
             "username": "JohnDoe" ,
             "email": "john.doe@ubermail.com",
             "password": "Scecret password"
        },
        {
             "id": 1,
             "username": "John Doe" ,
             "email": "john.doe@ubermail.com",
             "password": "S"
        }
    ]
}
```

## Reponse - server --> client

**Format**

`id` Indentifies a unique request-response-pair.
`type` What type of response this is. Always postfixed with `_response`.
`success` An array the items which were successfully processed.
`success[].id` Should match a corresponding `id` in the request payload.
`error` items which experienced an error during processing.
`error[].id` Should match a corresponding `id` in the request payload.
`error[].code` Array of error codes as strings. Guaranteed to always have one or more.

```
{   
    "id": <id>,
    "type": "<type>_response",
    "success": [
        {"id": <id>, ...<data>}
        ...
    ],
    "error": [
        {"id": <id>, "code": [<code1>, <code2>, ...]}
        ...
    ]
}
```

**Example**
```json
{
    "id": 0,
    "type": "create_user_response",
    "success": [
        { "id": 0 , "user_id": 2}
    ],
    "error": [
        { "id": 1, "code": ["invalid_username", "email_already_exists", 
                            "invalid_password"]}
    ]
}
```

## Event - server --> client


Events are one-way communication from the server to clients. You can think of it like push-notifications. The server does not want response from client. Examples of an event is: Game invite, game updated, game completed, chat invite, chat updated, chat message...

**Format**
`type` What type of event this is.
`payload` An array of items containing application specific data
```
{
    "type": <type>,
    "payload": [
        { ...<data> },
        { ...<data> }
    ]
}
```

**Example**

```json
{
    "type": "game_invite",
    "payload": [
        {"inviter_id": 1, "game_id": 4},
        {"inviter_id": 1, "game_id": 4}
    ]
}
```


# Ludo API

## General errors

* `@error` does not generally include validation errors. For overview of validation errors see @see [ValidationRules](#Validation-rules) or [Error Constants](#Error-Constants)
* All API requests which `@requires authentication` may lead to these two errors `not_authenticated` and `not_authorized`.
* When something is not found via an `id` a `<something>_not_found` error is returned.

## Payload limits and Response Length
* A request payload can contain up to 100 items
* Range requests can only contain 10 page requests.
* A page has a maximum limit of 100 items
* In the case where a payload has more than the maximum number of items, those items will not be processed but be reported as errors upon response.

## User API

### create_user

`@brief` Creates a user with the given username, email and password hash.
`@error` not_unique_username, malformed_username, not_unique_email, malformed_email

create_user_request
```json
{
    "id": 0,
    "type": "create_user_request",
    "payload": [
        {
             "id": 0,
             "username": "JohnDoe" ,
             "email": "john.doe@ubermail.com",
             "password": "Scecret password"
        },
        {
             "id": 1,
             "username": "John Doe" ,
             "email": "john.doe@ubermail.com",
             "password": "S"
        }
    ]
}
```

create_user_response
```json
{
    "id": 0,
    "type": "create_user_response",
    "success": [
        { "id": 0, "user_id": 2 }
    ],
    "error": [
        { "id": 1, "code": ["not_unique_email"]}
    ]
}
```


### login

`@brief` Login a user using email and password. If already logged in, refresh `auth_token`, practically logging out all currently logged in clients. Already logged in is not an error.
`@error` invalid_username_or_password

login_request
```json
{
    "id": 1,
    "type": "login_request",
    "payload": [
        { 
            "id": 0,
            "email": "jonas.solsvik@gmail.com", 
            "password": "Secret Password" 
        },
        { 
            "id": 1,
            "email": "jonas.solsvk@gmail.com", 
            "password": "ecret Passworz" 
        }
    ]
}
```

login_response
```json
{
    "id": 1,
    "type": "login_response",
    "success": [
        {"id": 0, "user_id": 2, "auth_token": "f136803..." }
    ],
    "error": [
        {"id": 1, "code": ["invalid_username_or_password"]}
    ]
}
```


### logout

`@requires` authentication
`@brief` Logout user. Idempotent. Already logged out is not an error.

logout_request
```json
{
    "id": 2,
    "type": "logout_request",
    "payload": [
        {"id": 0, "user_id": 1},
        {"id": 1, "user_id": 2}
    ],
    "auth_token": "f13680.."
}
```

logout_response
```json
{
    "id": 2,
    "type": "logout_response",
    "success": [
        {"id": 0}
    ],
    "errors": [
        {"id": 1, "code":["unauthorized"]}
    ]
}
```


### get_user

`@requires` authentication
`@brief` Returns information about the requested users containing: `user_id`, `username` and `avatar_uri`.
Note: All fields are guaranteed to be present, but `avatar_uri` might be an empty string.

get_user_request (The token belongs to user_id: 3)
```json
{
    "id": 3,
    "type": "get_user_request",
    "payload": [
        {"id": 0, "user_id": 3},
        {"id": 1, "user_id": 4}
    ],
    "auth_token": "f1368.." 
}
```

get_user_response
```json
{
    "id": 3,
    "type": "get_user_response",
    "success": [
        {
            "user_id": 3, 
            "username": "John Doe", 
            "avatar_uri": "http://imgur.com/myavatar", 
        },
        {
            "user_id": 4, 
            "username": "Jenna", 
            "avatar_uri": "http://imgur.com/myavatar", 
        },
    ],
    "error": [
    ]
}
```

### get_user_range

`@requires` authentication
`@brief` - Get a range of users.
In the case where a page is "out of bounds", no error is given,
the range array just does not contain any items.

get_user_range
```json 
{
    "id": 19,
    "type": "get_user_range_request",
    "payload": [
        {"id": 0, "page_index": 0}
        {"id": 1, "page_index": 99}
    ],
    "auth_token": "f4029..",
}
```

get_user_range_response
```json 
{
    "id": 19,
    "type": "get_user_range_response",
    "success": [
        {
            "id": 0,
            "range":
            [
                {
                    "user_id": 3, 
                    "username": "John Doe", 
                    "avatar_uri": "http://imgur.com/myavatar"
                },
                {
                    "user_id": 4, 
                    "username": "Jenna", 
                    "avatar_uri": "http://imgur.com/myavatar"
                },
            ]
        },
        {
            "id": 1,
            "range": []
        }
    ],
    "error": [
    ]
}
```


### update_user
 
`@requires` authentication
`@brief` Update user ainformation.
`@error` not_unique_username, not_unique_email

update_user_request
```json
{
    "id": 4,
    "type": "update_user_request",
    "payload": [
        { 
             "id": 0,
             "user_id": 2,
             "username": "JohnDoe" ,
             "email": "john.doe@ubermail.com",
             "password": "New Secret Password",
             "avatar_uri": "http://imgur.com/myavatar"
        },
        { 
             "id": 1,
             "user_id": 3,
             "username": "Jenna Doe" ,
             "email": "john.doe@ u bermail.com",
             "password": "New Sec ret Password",
             "avatar_uri": "http://imgur.com/myavatar"
        }
    ],
    "auth_token": "f13680.."
}
```

update_user_response
```json
{
    "id": 4,
    "type": "update_user_response",
    "success": [
        {"id": 0}
    ],
    "error": [
        {"id": 1, "code": ["not_unique_email"]}
    ]
}
```


### delete_user

`@requires` authentication
`@brief` Deletes user
`@error` user_not_found

delete_user_request
```json
{
    "id": 5,
    "type": "delete_user_request",
    "payload": [
        {"id": 0, "user_id": 3 },
        {"id": 1, "user_id": 2 }
    ],
    "auth_token": "f1368.." 
}
```

delete_user_response
```json
{
    "id": 5,
    "type": "delete_user_response",
    "success": [
        { "id": 0 }
    ], 
    "error": [
        {"id": 1, "code":["unauthorized"]}
    ]
}
```


## Friends API

### get_friend_range

`@requires` authentication
`@brief` - Get a range of friends.
Different status numbers means:
`0: friended`
`1: ignored`
`2: pending`

get_friend_range
```json 
{
    "id": 19,
    "type": "get_friend_range_request",
    "payload": [
        {"id": 0, "user_id": 0, "page_index": 0}
        {"id": 1, "user_id": 1, "page_index": 99}
    ],
    "auth_token": "f4029..",
}
```

get_friend_range_response
```json 
{
    "id": 19,
    "type": "get_friend_range_response",
    "success": [
        {
            "id": 0,
            "range":
            [
                { "user_id": 2, "username": "karl", "status": "accepted" },
                { "user_id": 3, "username": "jonas", "status": "pending" },
            ]
        },
        {
            "id": 1,
            "range":[]
        }
    ],
    "error": [
    ]
}
```


### friend

`@requires` authentication
`@brief` Friend someone. Both users has to friend each other, to actually be friends. If only one of the users has friended the other, the friend is just a pending.
`@error` user_not_found | other_id_not_found | user_and_other_id_is_same



friend_request
```json
{
    "id": 7,
    "type": "friend_request",
    "payload": [
        {"id": 0, "user_id": 3, "other_id": 4},
        {"id": 1, "user_id": 4, "other_id": 5},
    ],
    "auth_token": "f1368.." 
}
```

friend_response
```json
{
    "id": 7,
    "type": "friend_response",
    "success": [
        {"id": 1}
    ],
    "error": [
        {"id": 0, "code": ["user_not_found"]}
    ]
}
```

### unfriend

`@requires` authentication
`@brief` Delete the friendship between user_id and other_id.
`@error` user_not_found | friend_not_found

unfriend_request
```json
{
    "id":  8,
    "type": "unfriend_request",
    "payload": [
        {"id": 0, "user_id": 3, "other_id": 4},
        {"id": 1, "user_id": 3, "other_id": 5}
    ],
    "auth_token": "f1368.." 
}
```

unfriend_response
```json
{
    "id":  8,
    "type": "unfriend_response",
    "success": [
        {"id": 1}
    ],
    "error": [
        {"id": 0, "code": ["friend_not_found"]}
    ]
}
```

### ignore
`@requires` authentication
`@brief` Ignores the specified user, so they cannot send friend requests anymore.
         You cannot ignore friends, you need to unfriend them first.
`@error` user_not_found | user_is_friend

ignore_request
```json
{
    "id":  8,
    "type": "ignore_request",
    "payload": [
        {"id": 0, "user_id": 3, "other_id": 4},
        {"id": 1, "user_id": 3, "other_id": 5}
    ],
    "auth_token": "f1368.." 
}
```

ignore_response
```json
{
    "id":  8,
    "type": "ignore_response",
    "success": [
        {"id": 1}
    ],
    "error": [
        {"id": 0, "code": ["user_not_found"]}
    ]
}
```

## Chat API

### create_chat

`@requires` authentication
`@brief` - Create chat with given name

create_chat_request
```json
{    
    "id": 9,
    "type": "create_chat_request",
    "success": [
        {"id": 0, "user_id": 2, "name": "My cool chat"}
    ],
    "auth_token": "f4029..",
}
```

create_chat_response
```json
{
    "id": 9,
    "type": "create_chat_response",
    "success": [
        {"id": 0, "chat_id": 4}
    ],
    "errors": []
}
```

### join_chat

`@requires` authentication
`@brief` join chosen chats
`@error` access_denied

join_chat_request
```json
{
    "id": 10,
    "type": "join_chat_request",
    "payload": [
        {"id": 0, "user_id": 3, "chat_id": 2},
        {"id": 1, "user_id": 4, "chat_id": 1},
        {"id": 2, "user_id": 4, "chat_id": 3},
        {"id": 3, "user_id": 4, "chat_id": 8}
    ],
    "auth_token": "f4029.."
}
```

join_chat_response
```json
{
    "id": 10,
    "type": "join_chat_response",
    "success": [
        {"id": 0},
        {"id": 1},
        {"id": 2}
    ],
    "errors": [
        {"id": 3, "code": ["access_denied"]}
    ]
}
```

### leave_chat

`@requires` authentication
`@brief` leave an ongoing chat session

leave_chat_request
```json
{
    "id": 10,
    "type": "leave_chat_request",
    "payload": [
        {"id": 0, "chat_id": 2},
        {"id": 1, "chat_id": 3}
    ],
    "auth_token": "f4029.."
}
```

leave_chat_response
```json
{
    "id": 10,
    "type": "leave_chat_response",
    "success": [
        {"id": 1}
    ],
    "error": [
        {"id": 0, "code": ["chat_not_found"]}
    ]
}
```



### get_chat

`@requires` authentication
`@brief` - Gets all the non-game chats.

get_chat_request
```json
{
    "id": 11,
    "type": "get_chat_request",
    "payload": [
        {"id": 0, "chat_id": 2}
        {"id": 1, "chat_id": 3}
    ],
    "auth_token": "f4029.."
}
```


get_chat_response
```json
{
    "id": 11,
    "type": "get_chat_response",
    "success": [
        {"id": 0, "chat_id": 2, "name": "my cool chat", "participant_id": [1, 2, 3] },
        {"id": 1, "chat_id": 3, "name": "my cool chat2", "participant_id": [1, 3, 4] }
    ],
    "error": []
}
```

### send_chat_message

`@requires` authentication
`@brief` - Send message to all participants of any active chat

send_chat_message_request
```json
{
    "id": 12,
    "type": "send_chat_message_request",
    "payload": [
        {"id": 0, "user_id": 3, "chat_id": 2, "message": "Hei"},
        {"id": 1, "user_id": 3, "chat_id": 2, "message": "på" }
    ],
    "auth_token": "f4029.."
}
```

send_chat_message_response
```json
{
    "id": 12,
    "type": "send_chat_message_response",
    "success": [
        {"id": 0}
    ],
    "error": [
        {"id": 1, "code": ["message_to_long"]}
    ]
}
```

### send_chat_invite

`@requires` authentication
`@brief` - Send chat invite to friend

send_chat_invite_request
```json
{
    "id": 13,
    "type": "send_chat_invite_request",
    "payload": [
        {"id": 0, "user_id": 2, "other_id": 1, "chat_id": 4},
        {"id": 1, "user_id": 2, "other_id": 2, "chat_id": 3}
    ],
    "auth_token": "f4029.."
}
```

send_chat_invite_response
```json
{
    "id": 13,
    "type": "send_chat_invite_response",
    "success": [
        {"id": 0},
        {"id": 1}
    ],
    "error": [],
}
```


## Game API

### create_game

`@requires` authentication
`@Brief` - Create a game of Ludo. Afterwards you can invite users.

create_game_request
```json
{
    "id": 14,
    "type": "create_game_request",
    "payload": [
        {"id": 0, "user_id": 1, "name": "my game"},
        {"id": 1, "user_id": 1, "name": "my game2"},
        {"id": 2, "user_id": 59, "name": "my game"}
    ],
    "auth_token": "f4029..",
}
```

create_game_response
```json
{
    "id": 14,
    "type": "create_game_response",
    "success": [
        {"id": 0, "game_id": 0},
        {"id": 1, "game_id": 1}
    ],
    "error": [
        {"id": 2, "code": ["user_id_not_found"]}
    ]
}
```

### send_game_invite

`@requires` authentication
`@Brief` - Send game invite to friend

send_game_invite_request
```json
{
    "id": 15,
    "type": "send_game_invite_request",
    "payload": [
        {"id": 0, "user_id": 3, "other_id": 1, "game_id": 5},
        {"id": 1, "user_id": 2, "other_id": 1, "game_id": 4}
    ],
    "auth_token": "f4029.."
}
```

send_game_invite_response
```json 
{
    "id": 15,
    "type": "send_game_invite_response",
    "success": [
        {"id": 1}
    ],
    "error": [
        {"id": 0, "code": ["user_not_found"]}
    ]
}
```

### decline_game_invite

`@requires` authentication

decline_game_invite_request
```json
{
    "id": 16,
    "type": "decline_game_invite_request",
    "payload": [
        {"id": 0, "user_id": 0, "game_id": 0},
    ]
}
```

decline_game_invite_response
```json 
{
    "id": 16,
    "type": "decline_game_invite_response",
    "success": [
        {"id": 0}
    ],
    "error": [],
}
```

### join_game

`@requires` authentication
`@brief` - join chosen games
`@triggers` - game_update

join_game_request
```json
{
    "id": 17,
    "type": "join_game_request",
    "payload": [
        {"id": 0, "user_id": 3, "game_id": 2},
        {"id": 1, "user_id": 3, "game_id": 1},
        {"id": 2, "user_id": 3, "game_id": 3},
        {"id": 3, "user_id": 3, "game_id": 8}
    ],
    "auth_token": "f4029..",
}
```

join_game_response
```json
{
    "id": 17,
    "type": "join_game_response",
    "success": [
        {"id": 0},
        {"id": 1},
        {"id": 3}
    ],
    "errors": [
        {"id": 2, "code": ["access_denied"]}
    ]
}
```

### leave_game

`@requires` authentication
`@brief` - leaves the chosen games
`@triggers` - game_update

join_game_request
```json
{
    "id": 17,
    "type": "leave_game_request",
    "payload": [
        {"id": 0, "user_id": 3, "game_id": 2},
        {"id": 1, "user_id": 3, "game_id": 1},
        {"id": 2, "user_id": 3, "game_id": 3},
        {"id": 3, "user_id": 3, "game_id": 8}
    ],
    "auth_token": "f4029..",
}
```

join_game_response
```json
{
    "id": 25,
    "type": "leave_game_response",
    "success": [
        {"id": 0},
        {"id": 1},
        {"id": 3}
    ],
    "errors": [
        {"id": 2, "code": ["access_denied"]}
    ]
}
```

### start_game

`@requires` authentication
`@brief` - Start game of Ludo with 2 or more players. The user who starts the game has to be the owner/creator of the game.
`@triggers` - game_update
`@error` not_enough_players

start_game_request
```json 
{
    "id": 18,
    "type": "start_game_request",
    "payload": [
        {"id": 0, "game_id": 0},
        {"id": 1, "game_id": 1}
    ],
    "auth_token": "f4029.."
}
```

start_game_response
```json 
{
    "id": 18,
    "type": "start_game_response",
    "success": [
        {"id": 0},
    ],
    "error": [
        {"id": 1, "code": ["not_enough_players"]}
    ]
}
```

### get_game

`@requires` authentication
`@brief` - Get metainformation from games with id. `owner_id`, `player_id` and `pending_id` are all the respective users `user_id`'s.
`@status` - Status is a numerical constant. 0 for LOBBY, 1 for INSESSION, 2 for GAME OVER

get_game_request
```json 
{
    "id": 19,
    "type": "get_game_request",
    "payload": [
        {"id": 0, "game_id": 0}
        {"id": 1, "game_id": 1}
    ],
    "auth_token": "f4029..",
}
```

get_game_response
```json 
{
    "id": 19,
    "type": "get_game_response",
    "success": [
        {
            "id": 0,
            "name": "Game 1st",
            "game_id": 0, 
            "status": 0,
            "owner_id": 2,
            "player_id": [58, 2, 4, 7],
            "pending_id": [1]
        },
        {
            "id": 1,
            "name": "Game 2nd",
            "game_id": 1,
            "status": 1,
            "owner_id": 3,
            "player_id": [58, 3, 4],
            "pending_id": [1]
        }
    ],
    "error": [
    ]
}
```

### send_roll_dice

`@requires` authentication
`@brief` - The player wants to roll the dice
`@triggers` - game_update

roll_dice_request
```json 
{
    "id": 20,
    "type": "roll_dice_request",
    "payload": [
        {"id": 0, "user_id": 0, "game_id": 0},
        {"id": 1, "user_id": 0, "game_id": 1},
        {"id": 2, "user_id": 0, "game_id": 2},
        {"id": 3, "user_id": 0, "game_id": 3}
    ],
    "auth_token": "f4029..",
}
```

roll_dice_response
```json 
{
    "id": 20,
    "type": "roll_dice_response",
    "success": [
        {"id": 1},
        {"id": 2},
        {"id": 3}
    ],
    "error": [
        {"id": 0, "code": ["not_your_turn"]},
    ]
}
```

### send_move_piece

`@requires` authentication
`@triggers` - game_update

move_piece_request
```json 
{
    "id": 21,
    "type": "move_piece_request",
    "payload": [
        {"id": 0, "user_id": 0, "game_id": 0, "piece_index": 0},
        {"id": 1, "user_id": 1, "game_id": 1, "piece_index": 3}
    ],
    "auth_token": "f4029.."
}
```


move_piece_response
```json 
{
    "id": 21,
    "type": "move_piece_response",
    "success": [
        {"id": 1}
    ],
    "error": [
        {"id": 0, "code": ["illegal_move_for_piece"]}
    ]
}
```

## Events API

* Server event pushed to client.
* One way communication.
* Server does not want response from client.
* Generally an event only carry id, not actual data.
* Data fetched from a separate get-request, using the id.

### friend_update

`@brief` - Friend data is updated.

```json 
{
    "type": "friend_update",
    "payload": [
    ]
}
```

### chat_update

`@brief` - Chat you are in has been updated

```json
{
    "type": "chat_update",
    "payload": [
        {"chat_id": 2},
        {"chat_id": 3},
    ]
}
```


### chat_invite
`@Brief` - Someone invited you to a chat.

```json
{
    "type": "chat_invite",
    "payload": [
        {"user_id": 2, "chat_id": 4},
    ]
}
```

### chat_message
`@brief` - Someone sent a new chat message.

chat_message
```json
{
    "type": "chat_message",
    "payload": [
        {"user_id": 3, "chat_id": 2, "message": "Hei"},
        {"user_id": 3, "chat_id": 2, "message": "på" }
    ]
}
```

### game_update
`@brief` - Game you are in has been updated

```json 
{
    "type": "game_update",
    "payload": [
        {"game_id": 2},
        {"game_id": 3},
    ]
}
```

### game_invite
`@brief` - Someone sent you a game invite.

```json
{
    "type": "game_invite",
    "payload": [
        {"user_id": 1, "game_id": 4},
        {"user_id": 1, "game_id": 4}
    ]
}
```

### game_state_update
`@requires` authentication
`@brief` - Sent every time the gamestate is updated.
`piece_positions` is a 2d array containing the positions of the 4 pieces belonging to each player. 
`@status` 1 - indicating insession, 2 - indicating "gameover"
`@status` Either a user_id, or -1 if no one has won.

game_state_update
```json 
{
    "type": "game_state_update",
    "payload": [ 
        // In this game player 58 threw a 4, and will move its piece
        {
            "game_id": 1,
            "player_order": [58, 3, 4, 7],
            "current_player_id": 58,
            "next_action": move,
            "previous_dice_throw": 4,
            "piece_positions": [
                [ 3, 1,  3,  5],    // player 1
                [ 7, 7,  7,  7],    // player 2
                [32, 2,  0,  0],    // player 3
                [ 0, 1, 17, 19]     // player 4
            ],
            "status": 1
            "winner": -1
        }
    ]
}
```


### force_logout

`@brief` - Sent to currently logged in client, when a user logs in on another client.

```json
{
    "type": "force_logout",
    "payload":[]
}
```


## Error Constants
In the case an error occurs you should use the explicit enum values supplied by the Error class. The actual numerical codes for the different error values are ommited to avoid implementers using them rather than the constants.

**invalid_type**
Does not match validation rule for `type`.

**invalid_id**
Does not match validation rule for `id`.

**invalid_email**
Does not match validation rule for `email`.

**invalid_username**
Does not match validation rule for `username`.

**invalid_password**
Does not match validation rule for `password`.

**email_or_password_incorrect**
Either eamil or pasword is incorrect. We do not tell which one of them.

**authentication_failed**
Token failed to authenticate client.

**authorization_failed**
The authenticated client was not authorized to do operation.

**email_already_exists**
Another user with that email already exists.

**\<keyword\>_id_not_found**
The suplied id was not found


## Validation Rules

**id and \<keyword>_id**
* positive integer
* min 0
* max 9,223,372,036,854,775,807
* @throws `invalid_id`

**type**
* enumeration type 
* @see ludo/enum/MessageType.java
* @throws `invalid_type`

**friend_status** 
* enumeration type
* pending | friend | unfriend 
* @see ludo/enum/FriendStatus.java
* @throws `invalid_friend_status`

**next_move**
* enumeration type
* move | roll
* @see ludo/enum/NextMove.java
* @error `invalid_next_move`

**username** 
* Latin letters only. 
* No whitespace. 
* Length <= 32
* @throws `invalid_username`

**email**
* @TODO insert regex here
* string containing exactly a single --> `@` <-- or 
* no whitespace
* contain atleast one .
* @throws `invalid_email`

**password**
* length >= 12 characers.
* No other requirements, length is enough.
* throws `invalid_password`



