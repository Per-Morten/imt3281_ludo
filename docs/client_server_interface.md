# TODOS
* [x] TODO: Skal vi bytte ut action med: Type?
* [x] TODO: Skal vi ha PascalCase eller snake_case på feilkoder og action navn? snake_case i json, og PascalCase i Java.
* [ ] TODO: Når skal vi sende user_id, når skal vi ikke?
* [ ] TODO: Sikre at alle error koder kan direkte knyttes til det objektet det er snakk om.
* [ ] TODO: Sikre at vi er konsekvente på når det er sub-arrayer med objekter med navngitte attributter vs sub-array med verdier.
* [ ] TODO: Oppdater alle til å sende med id.
* [ ] TODO: Spør kolloen om tynnklienter?
* [ ] TODO: Ska vi bruke s? nei, alle skal være entall, da alle meldinger kan inneholde flere objekter som skal handles på.
* [ ] TODO: Fjern alle @examples tags
* [ ] TODO: Skriv @brief på alt?
* [ ] TODO: Skriv @errors
* [ ] TODO: Endre payload navnet på alle request_responses til success.
* [ ] TODO: sjekk @requires authentication
* [ ] TODO: Oppdater alle til å sende inn user_id.

>Kan vi ha det slik at all spillogikk ligger på serveren?
Det eneste klienten gjør er å vise spillets tilstand. Den tar ingen avgjørelser (annet enn å spørre om å kaste terning og flytte en brikke). Hver gang spillets tilstand endrer seg, så sender serveren oppdatert data på nytt til klienten.
Serveren tar avgjørelser om hva som er lov og ikke. Serveren er den eneste som endrer på spillets tilstand.
Dette medfører at vi ikke tar i bruk Ludo-klassen på klient, men bare på server.

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

## Communication protocol
Communication either happens through 'one-way' data transfers, or 'two-way' transactions in the form of a request. 

* JSON-only protocol.
* The payload could be array or object (`@todo` discuss if we should only allow array to simplify)

### Request Responses
All requests expects a response, either in the form of a success, or an error. The 'type' of the response will be the 'type' name of the request (where \_request is trimmed) postfixed with either \_success or \_error.

### Success Responses
In the cases where a request succeeds, the data within the payload is interpreted based on the 'type' itself. 
Unless otherwise noted, all responses are guaranteed to have at least one element in their payload.

### Error Responses
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
### Error Constants
In the case an error occurs you should use the explicit enum values supplied by the Error class. The actual numerical codes for the different error values are ommited to avoid implementers using them rather than the constants.

*invalid_email*
Occurs when the supplied email for a create_user request does not meet the requirements of an email. I.e. It fails the regex for valid emails.

*username_too_long*
Occurs when the supplied username is too long to be put into the database.

*invalid_token*
Ocurs when a token has timed out or otherwise is invalid.

`@todo` How should we respond that the password supplied wasn't correct? Do we just have a generic: Login failed because of username password missmatch?

### Verify token

A verify token may be used in a request - reponse pair, to match a response with request.

>Forslag: Kvart request-response-par har en request_token . Request_token blir alene brukt til å verifisere hvilke responses som hører til hvilke requests. Det er derfor viktig at hvert request_token er unikt. Eksempelvis kan request_token produseres av klienten på denne måten request_token = hash( microtime() + client_secret );.  Her antar vi at microtime er unikt hver gang klienten lager en ny request.  client_secret  er for å sikre at request_token bare blir brukt som en token, og ikke som et tidspunkt.


## TODO: Explain why this is the generic response
```json
{
    "type": "join_chats_request",
    "payload": [],
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

create_user_request
```json
{
    "id": 0,
    "type": "create_user_request",
    "payload": [
        { 
             "id": 3
             "username": "JohnDoe" ,
             "email": "john.doe@ubermail.com",
             "password_hash": "6265b22b66502d70d5f004f08238ac3c"
        }    
    ]
}
```

create_user_response
```json
{
    "id": 0,
    "type": "create_user_success",
    "payload": [
        { "id": 0 }
    ],
    "error": []
}
```

### login

login_request
```json
{
    "id": 1,
    "type": "login_request",
    "payload": [
        { 
            "id": 0,
            "email": "jonas.solsvik@gmail.com", 
            "password_hash": "6265b22b66502d70d5f004f08238ac3c" 
        }
    ]
}
```

login_response
```json
{
    "id": 1,
    "type": "login_response",
    "payload": [
        {"id": 0, "user_id": 2, "token": "f136803ab9c241079ba0cc1b5d02ee77" }
    ]
}
```

### logout
Note: No elements in the payload is required, but the payload variable is still required.
`@requires` authentication

`@Examples`

logout_request
```json
{
    "id": 2,
    "type": "logout_request",
    "payload": [
        {"id": 0, "user_id": 1},
        {"id": 1, "user_id": 2}
    ],
    "token": "f13680.."
}
```

logout_response
```json
{
    "id": 2,
    "type": "logout_response",
    "payload": [
        {"id": 0}
    ],
    "errors": [
        {"id": 1, "codes":["not_authorized"]}
    ]
}
```

### update_user

`@brief` Update user information. 
`@requires` authentication
`@examples`

update_user_request
```json
{
    "id": 3,
    "type": "update_user_request",
    "payload": [
        { 
             "id": 0,
             "user_id": 2,
             "username": "JohnDoe" ,
             "email": "john.doe@ubermail.com",
             "password_hash": "6265b22b66502d70d5f004f08238ac3c"
        }
    ],
    "token": "f13680.."
}
```
update_user_request

@TODO implement


### delete_user

`@brief` Delete user

delete_user_request
```json
{
    "id": 4,
    "type": "delete_user_request",
    "payload": [
        {"id:" 0, user_id": 3 }
        {"id:" 1, user_id": 2 }

    ],
    "token": "f1368.." 
}
```

delete_user_success
```json
{
 
 "id": 4,
    "type": "delete_user_success",
    "payload": [
        { "id": 0 }
    ], 
    "error": [
        {"id": 1, "codes":["unathorized"]}
    ]
}
```

### get_user
`@brief` Get users.
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

### get_friend

`@brief`
`@requires` authentcation

`@examples`
get_friend_request
```json
{
    "type": "get_friend_request",
    "payload": [
        {"user_id": 3},
    ],
    "token": "f1368.." 
}
```

get_friend_response
```json
{
    "type": "get_friend_response",
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

`@brief` Unfriend can also be used to ignore pending friend requests.
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



## Chat API

### create_chat

`@brief` - Create chat with given name
`@requires` authentication

`@Examples`

create_chat_request
```json
{    
    "id": 3,
    "type": "create_chat_request",
    "payload": [
        {"id": 0, "user_id": 2, "name": "My cool chat"}
    ],
    "token": "f4029..",
}
```

create_chat_response
```json
{
    "id": 3,
    "type": "create_chat_response",
    "payload": [
        {"id": 0, chat_id": 4}
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
        {"id": 0, "user_id": 3, "chat_id": 2},
        {"id": 1, "user_id": 4, "chat_id": 1},
        {"id": 2, "user_id": 4, "chat_id": 3},
        {"id": 3, "user_id": 4, "chat_id": 8}
    ],
    "token": "f4029.."
}
```

join_chats_response
```json
{
    "type": "join_chats_response",
    "payload": [
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
`@brief` - Gets all the non-game chats. If no chat_id is supplied, all chats are returned.
`@requires` - authentication

`@Examples`

get_chat_request
```json
{
    "type": "get_chat_request",
    "payload": [
        {"chat_id": 2}
    ],
    "token": "f4029.."
}
```


get_chat_response
```json
{
    "type": "get_chat_response",
    "payload": [
        {"chat_id": 2, "name": "my cool chat", "participant_id": [1, 2, 3] },
        {"chat_id": 3, "name": "my cool chat2", "participant_id": [1, 3, 4] }
    ],
    "errors": []
}
```

### send_chat_message
`@brief` - Send message to all participants of any active chat
`@requires` - authentication

`@example`

send_chat_message_request
```json
{
    "id": 0,
    "type": "send_chat_message_request",
    "payload": [
        {"id": 0, user_id": 3, "chat_id": 2, "message_id": "ff88109i3", "message": "Hei"},
        {"id": 1, user_id": 3, "chat_id": 2, "message_id": "ff0193940", "message": "på" }
    ],
    "token": "f4029.."
}
```

send_chat_message_response
```json
{
    "id": 0,
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

`@Brief` - Send chat invite to friend
`@Constraint` - `fromId` has to be the id of friend

`@example`

send_chat_invite_request
```json
{
    "type": "send_chat_invite_request",
    "payload": [
        {"user_id": 2, "invitee": 1, "chat_id": 4, "invite_id": "ff88109i3"},
        {"user_id": 2, "invitee": 2, "chat_id": 3, "invite_id": "ff0193940"},
    ],
    "token": "f4029.."
}
```

send_chat_invite_response
```json
{
    "type": "send_chat_invite_response",
    "payload": [
        {"invite_id": "ff88109i3"},
        {"invite_id": "ff0193940"},
    ],
    "errors": [],
}
```


## Game API

### create_game
`@Brief` - Create a game of Ludo. Decied how many participants you want.
`@Errors` - max_participants_not_unsigned_integer


create_game_request
```json
{
    "type": "create_game_request",
    "payload": [
        {
            "user_id": 1,
            "game_id": "ff88109i3",
            "max_participants": 4,
        },
        {
            "user_id": 1,
            "game_id": "ff88109i4", 
            "max_participants": 2,
        },
        {
            "user_id": 1,
            "game_id": "ff0193940", 
            "max_participants": -2,
        }
    ],
    "user_token": "f4029..",
    "request_id": 3
}
```

create_game_response
```json
{
    "type": "create_game_response",
    "payload": [
        {"game_id": "ff88109i3"},
        {"game_id": "ff88109i4"}
    ],
    "errors": [
        {"game_id": "ff0193940", "codes": ["max_participants_not_unsigned_integer"]}
    ],
    "request_id": 3
}
```

### send_game_invite

`@Brief` - Send game invite to friend

send_game_invite_request
```json
{
    "type": "send_game_invite_request",
    "payload": [
        {"user_id": 2, "invitee_id": 1, "game_id": 4, "game_invite_id": "ff88109i3"}
    ],
    "token": "f4029..",
    "request_id": 4
}
```

send_game_invite_response
```json 
{
    "type": "send_game_invite_response",
    "payload": [
    ],
    "errors": [
        {"game_invite_id": "ff88109i3", "codes": ["user_does_not_exist"]}
    ],
    "request_id": 4
}
```

### decline_game_invite

decline_game_invite_request
```json
{
    "type": "decline_game_invite_request",
    "payload": [
        {"user_id": 2, "game_invite_id": "ff88109i3"},
    ],
    "request_id": 5
}
```

decline_game_invite_response
```json 
{
    "type": "decline_game_invite_response",
    "payload": [
        {"game_invite_id": "ff88109i3"},
    ],
    "errors": [],
    "request_id": 5
}
```

### join_games

`@brief` - join chosen games
`@requires` authentication
`@triggers` - game_update

join_games_request
```json
{
    "type": "join_games_request",
    "payload": [
        {"user_id": 3, "game_id": 2},
        {"user_id": 3, "game_id": 1},
        {"user_id": 3, "game_id": 3},
        {"user_id": 3, "game_id": 8}
    ],
    "token": "f4029..",
    "request_id": 6
}
```

join_games_response
```json
{
    "type": "join_games_response",
    "payload": [
        {"game_id": 2},
        {"game_id": 1},
        {"game_id": 8}
    ],
    "errors": [
        {"chat_id": 3, "codes": ["access_denied"]}
    ],
    "request_id": 6
}
```


### start_games
`@brief` - Start game of Ludo with 2 or more players.
`@constraint` - The user who starts the game has to be the owner/creator of the game.
`@triggers` - game_update


start_games_request
```json 
{
    "type": "start_games_request",
    "payload": [
        {"user_id": 2, "game_id": "ffa08fj"},
        {"user_id": 2, "game_id": "ffa08tk"}

    ],
    "token": "f4029..",
    "request_id": 6
}
```

start_games_response
```json 
{
    "type": "start_games_response",
    "payload": [
        {"game_id": "ffa08fj"},
    ],
    "errors": [
        {"game_id": "ffa08tk", "codes": ["not_enough_players"]}
    ],
    "request_id": 6
}
```

### get_games

`@brief` - Get metainformation from games with id

get_games_request
```json 
{
    "type": "get_games_request",
    "payload": [
        {"game_id": "ffa08fj"},
        {"game_id": "ffa0840"}
    ],
    "token": "f4029..",
    "request_id": 6
}
```

get_games_response
```json 
{
    "type": "start_game_response",
    "payload": [
        {
            "game_id": "ffa08fj", 
            "owner_id": 2, 
            "participant_ids": [2,3,4]
        }
    ],
    "errors": [
        {"game_id": "ffa0840", "codes": ["access_denied"]}
    ],
    "request_id": 6
}
```

### send_roll_dice

`@brief` - The player wants to roll the dice
`@triggers` - roll_dice

send_roll_dice_request
```json 
{
    "type": "send_roll_dice_request",
    "payload": [
        {"id": 0, "game_id": "ffa0840"},
        {"id": 1, "game_id": "ffa0jj6"},
        {"id": 2, "game_id": "ffa084g"},
        {"id": 3, "game_id": "ffa0jj3"}
    ],
    "token": "f4029..",
    "request_id": 7
}
```

send_roll_dice_response
```json 
{
    "id": 7,
    "type": "send_roll_dice_response",
    "payload": [
        {"id": 1},
        {"id": 2},
        {"id": 3}
    ],
    "errors": [
        {"id": 0, "codes": ["not_your_turn"]},
    ],
    "token": "f4029..",
}
```

### send_move_piece

`@triggers` - move_piece

move_piece_request
```json 
{
    "id": 8,
    "type": "move_piece_request",
    "payload": [
        {"id": 0, "game_id": "ff10849", "piece_id": 1},
        {"id": 1, "game_id": "ff10554", "piece_id": 5}
    ],
    "token": "f4029.."
}
```


move_piece_response
```json 
{
    "id": 8,
    "type": "move_piece_response",
    "payload": [
        {"id": 1}
    ],
    "errors": [
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

### chat_invites
`@Brief` - Recieve chat invite from any of my friends.

```json
{
    "type": "chat_invite",
    "payload": [
        {"user_id": 2, "invitee_id": 1, "chat_id": 4, "invite_id": "ff88109i3"},
    ]
}
```

### chat_messages
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

### game_invites
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
    "errors": [],
    "request_id": 8
}
```


### move_piece

`@brief` - undecided

```json 
{
    "type": "move_piece",
    "payload": [],
    "errors": [],
    "request_id": 8
}
```




