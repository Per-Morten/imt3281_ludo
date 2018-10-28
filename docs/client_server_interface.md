# Server-Client Communication


# Index

[TOC]


# TODOS

* [x] TODO: Skal vi bytte ut action med: Type?
* [x] TODO: Skal vi ha PascalCase eller snake_case på feilkoder og action navn? snake_case i json, og PascalCase i Java.
* [x] TODO: Når skal vi sende user_id, når skal vi ikke?
* [ ] TODO: Sikre at alle error koder kan direkte knyttes til det objektet det er snakk om.
* [ ] TODO: Sikre at vi er konsekvente på når det er sub-arrayer med objekter med navngitte attributter vs sub-array med verdier.
* [x] TODO: Oppdater alle til å sende med id.
* [ ] TODO: Spør kolloen om tynnklienter?
* [x] TODO: Ska vi bruke s? nei, alle skal være entall, da alle meldinger kan inneholde flere objekter som skal handles på.
* [x] TODO: Fjern alle @examples tags
* [ ] TODO: Skriv @brief på alt?
* [ ] TODO: Skriv @errors
* [x] TODO: Endre payload navnet på alle request_responses til success.
* [ ] TODO: sjekk @requires authentication
* [ ] TODO: Oppdater alle til å sende inn user_id.

>Kan vi ha det slik at all spillogikk ligger på serveren?
Det eneste klienten gjør er å vise spillets tilstand. Den tar ingen avgjørelser (annet enn å spørre om å kaste terning og flytte en brikke). Hver gang spillets tilstand endrer seg, så sender serveren oppdatert data på nytt til klienten.
Serveren tar avgjørelser om hva som er lov og ikke. Serveren er den eneste som endrer på spillets tilstand.
Dette medfører at vi ikke tar i bruk Ludo-klassen på klient, men bare på server.




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

```
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
    "type": "create_user_success",
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

## Note on general errors

* `@error` does not generally include validation errors. For overview of validation errors see @see [ValidationRules](#Validation-rules) or [Error Constants](#Error-Constants)
* All API requests which `@requires authentication` may throw these two errors `not_authenticated` and `not_authorized`.


## User API

### create_user

`@brief` Creates a user with the given username, email and password hash.
`@error` email_already_exists

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
        { "id": 1, "code": ["invalid_username", "email_already_exists", 
                            "invalid_password"]}
    ]
}
```


### login

`@brief` Login a user using email and password. If already logged in, refresh `auth_token`, practically logging out all currently logged in clients. Already logged in is not an error.
`@error` email_or_password_incorrect

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
        {"id": 1, "code": ["email_or_password_incorrect"]}
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
        {"id": 1, "codes":["not_authorized"]}
    ]
}
```


### get_user

`@requires` authentication
`@brief` The information you get on each user will vary depending on you authorization level. The fields you are not allowed to see are still present but empty. All users are allowed to see `username` and `avatar_uri`.

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
            "email": "john.doe@gmail.com"
        },
        {
            "user_id": 4, 
            "username": "Jenna", 
            "avatar_uri": "http://imgur.com/myavatar", 
            "email": ""
        },
    ],
    "error": [
    ]
}
```


### update_user
 
`@requires` authentication
`@brief` Update user ainformation.
`@error` user_not_found

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
             "password": "New Secret Password"
        },
        { 
             "id": 1,
             "user_id": 3,
             "username": "Jenna Doe" ,
             "email": "john.doe@ u bermail.com",
             "password": "New Sec ret Password"
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
        {"id": 1, "code": ["invalid_email"]}
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
        {"id": 1, "codes":["not_authorized"]}
    ]
}
```


## Friends API

### get_friend

`@requires` authentcation
`@brief` Get 1 or more friends from friendslist.
`@error` friend_not_found

get_friend_request
```json
{
    "id": 6,
    "type": "get_friend_request",
    "payload": [
        {"id": 0, "friend_id": 3},
        {"id": 1, "friend_id": 4},
        {"id": 2, "friend_id": 5}
    ],
    "auth_token": "f1368.." 
}
```

get_friend_response
```json
{
    "id": 6,
    "type": "get_friend_response",
    "success": [
        {
            "id": 1,
            "friend_id": 4, 
            "username": "Jenna", 
            "avatar_uri": "http://imgur.com/myavatar", 
            "email": "",
            "status": "friend"
        },
        {
            "id": 2,
            "friend_id": 5, 
            "username": "Garry", 
            "avatar_uri": "http://imgur.com/myavatar", 
            "email": "",
            "status": "pending"
        }
    ],
    "error": [
        {"id": 0, "code": ["friend_not_found"]}
    ]
}
```


### friend

`@requires` authentication
`@brief` Friend someone. Both users has to friend each other, to actually be friends. If only one of the users has friended the other, the friend is just a pending.
`@error` user_not_found | friend_not_found



friend_request
```json
{
    "id": 7,
    "type": "friend_request",
    "payload": [
        {"id": 0, "user_id": 3, "friend_id": 4},
        {"id": 1, "user_id": 4, "friend_id": 5},
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
`@brief` Unfriend can also be used to ignore other users trying to be your friend.
`@error` user_not_found | friend_not_found

unfriend_request
```json
{
    "id":  8,
    "type": "ufriend_request",
    "payload": [
        {"id": 0, "user_id": 3, "friend_id": 4},
        {"id": 1, "user_id": 3, "friend_id": 5}
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
        {"id": 3, "codes": ["access_denied"]}
    ]
}
```

### get_chat

`@requires` authentication
`@brief` - Gets all the non-game chats. If no chat_id is supplied, all chats are returned.

get_chat_request
```json
{
    "id": 11,
    "type": "get_chat_request",
    "payload": [
        {"id": 0, "chat_id": 2}
        {"id": 1, "chat_id": 2}
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
        {"id": 0, "user_id": 2, "invitee_id": 1, "chat_id": 4},
        {"id": 1, "user_id": 2, "invitee_id": 2, "chat_id": 3},
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
        {"id": 0, "invite_id": "ff88109i3"},
        {"id": 1, "invite_id": "ff0193940"},
    ],
    "error": [],
}
```


## Game API

### create_game
`@Brief` - Create a game of Ludo. Decied how many participants you want.
`@Errors` - max_participants_not_unsigned_integer


create_game_request
```json
{
    "id": 14,
    "type": "create_game_request",
    "payload": [
        {
            "id": 0,
            "user_id": 1,
            "game_id": "ff88109i3",
            "max_participants": 4,
        },
        {
            "id": 1,
            "user_id": 1,
            "game_id": "ff88109i4", 
            "max_participants": 2,
        },
        {
            "id": 2,
            "user_id": 1,
            "game_id": "ff0193940", 
            "max_participants": -2,
        }
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
        {"id": 0, "game_id": "0"},
        {"id": 1, "game_id": "1"}
    ],
    "error": [
        {"id": 2, "code": ["max_participants_not_unsigned_integer"]}
    ]
}
```

### send_game_invite

`@Brief` - Send game invite to friend

send_game_invite_request
```json
{
    "id": 15,
    "type": "send_game_invite_request",
    "payload": [
        {"id": 0, "user_id": 3, "invitee_id": 1, "game_id": 5},
        {"id": 1, "user_id": 2, "invitee_id": 1, "game_id": 4}
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
        {"id": 1, "game_invite_id": "ff88109i3"}
    ],
    "error": [
        {"id": 0, "code": ["user_not_found"]}
    ]
}
```

### decline_game_invite

decline_game_invite_request
```json
{
    "id": 16,
    "type": "decline_game_invite_request",
    "payload": [
        {"id": 0, "game_invite_id": "ff88109i3"},
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

### join_games

`@brief` - join chosen games
`@requires` authentication
`@triggers` - game_update

join_games_request
```json
{
    "id": 17,
    "type": "join_games_request",
    "payload": [
        {"id": 0, "user_id": 3, "game_id": 2},
        {"id": 1, "user_id": 3, "game_id": 1},
        {"id": 2, "user_id": 3, "game_id": 3},
        {"id": 3, "user_id": 3, "game_id": 8}
    ],
    "auth_token": "f4029..",
}
```

join_games_response
```json
{
    "id": 17,
    "type": "join_games_response",
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


### start_games
`@brief` - Start game of Ludo with 2 or more players. The user who starts the game has to be the owner/creator of the game.
`@triggers` - game_update
`@error` not_enough_players

start_games_request
```json 
{
    "id": 18,
    "type": "start_games_request",
    "payload": [
        {"id": 0, "game_id": "ffa08fj"},
        {"id": 1, "game_id": "ffa08tk"}
    ],
    "auth_token": "f4029.."
}
```

start_games_response
```json 
{
    "id": 18,
    "type": "start_games_response",
    "success": [
        {"id": 0},
    ],
    "error": [
        {"id": 1, "code": ["not_enough_players"]}
    ]
}
```

### get_games

`@brief` - Get metainformation from games with id

get_games_request
```json 
{
    "id": 19,
    "type": "get_games_request",
    "payload": [
        {"id": 0, "game_id": "ffa08fj"},
        {"id": 1, "game_id": "ffa0840"}
    ],
    "auth_token": "f4029..",
}
```

get_games_response
```json 
{
    "id": 19,
    "type": "start_game_response",
    "success": [
        {
            "id": 0,
            "game_id": "ffa08fj", 
            "owner_id": 2, 
            "participant_ids": [2,3,4]
        }
    ],
    "error": [
        {"id": 1, "codes": ["access_denied"]}
    ]
}
```

### send_roll_dice

`@brief` - The player wants to roll the dice
`@triggers` - roll_dice

send_roll_dice_request
```json 
{
    "id": 20,
    "type": "send_roll_dice_request",
    "payload": [
        {"id": 0, "game_id": "ffa0840"},
        {"id": 1, "game_id": "ffa0jj6"},
        {"id": 2, "game_id": "ffa084g"},
        {"id": 3, "game_id": "ffa0jj3"}
    ],
    "auth_token": "f4029..",
}
```

send_roll_dice_response
```json 
{
    "id": 20,
    "type": "send_roll_dice_response",
    "success": [
        {"id": 1},
        {"id": 2},
        {"id": 3}
    ],
    "error": [
        {"id": 0, "codes": ["not_your_turn"]},
    ]
}
```

### send_move_piece

`@triggers` - move_piece

move_piece_request
```json 
{
    "id": 21,
    "type": "move_piece_request",
    "payload": [
        {"id": 0, "game_id": "ff10849", "piece_id": 1},
        {"id": 1, "game_id": "ff10554", "piece_id": 5}
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
        {"id": 0, "codes": ["illegal_move_for_piece"]}
    ]
}
```

## Events API

Server events pushed to clients. 
One way messages. 
Server does not expect response from clients.
Events API does not data.

### friends_update
`@brief` - Notify about friends with new data

```json 
{
    "type": "friend_update",
    "payload": [
        { "user_id": 2},
        { "user_id": 3},
    ]
}
```

### chats_update
`@brief` - Notify about chats with new data. Meta information about the chat, such as new participants, name change, etc.

```json
{
    "type": "chats_update",
    "payload": [
        { "chat_id": 2},
        { "chat_id": 3},
    ]
}
```

### games_update
`@brief` - Notify about games with new data

```json 
{
    "type": "game_update",
    "payload": [
        { "game_id": 2},
        { "game_id": 3},
    ]
}
```

### chat_invite
`@Brief` - Recieve chat invite from any of my friends.

```json
{
    "type": "chat_invite",
    "payload": [
        {"user_id": 2, "invitee_id": 1, "chat_id": 4, "invite_id": "ff88109i3"},
    ]
}
```

### chat_message
`@brief` - Receive chat messages from chats i have joined

chat_message
```json
{
    "type": "chat_message",
    "payload": [
        {"user_id": 3, "chat_id": 2, "message_id": "ff88109i3", "message": "Hei"},
        {"user_id": 3, "chat_id": 2, "message_id": "ff0193940", "message": "på" }
    ]
}
```

### game_invite
`@brief` - Invites from friends to a ludo game

```json
{
    "type": "game_invite",
    "payload": [
        {"user_id": 1, "invitee_id": 1, "game_id": 4},
        {"user_id": 1, "invitee_id": 1, "game_id": 4}
    ]
}
```

### roll_dice 

`@brief` - undecided

```json 
{
    "type": "roll_dice",
    "payload": [],
}
```


### move_piece

`@brief` - undecided

```json 
{
    "type": "move_piece",
    "payload": [],

}
```

### force_logout

`@brief` - Happens to a currently logged in client, when a user logs in on another client.

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

**email_already_exists**
Another user with that email already exists.


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
* "pending" | "friend" | "unfriend" 
* @see ludo/enum/FriendStatus.java
* @throws `invalid_friend_status`

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


