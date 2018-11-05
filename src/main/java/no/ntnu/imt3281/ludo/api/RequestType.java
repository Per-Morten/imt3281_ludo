package no.ntnu.imt3281.ludo.api;

public enum RequestType {
    LoginRequest,
    LogoutRequest,
    GetUserRequest,
    CreateUserRequest,
    UpdateUserRequest,
    DeleteUserRequest,
    GetFriendRequest,
    FriendRequest,
    UnfriendRequest,
    JoinChatRequest,
    LeaveChatRequest,
    GetChatRequest,
    CreateChatRequest,
    SendChatMessageRequest,
    SendChatInviteRequest,
    CreateGameRequest,
    JoinGameRequest,
    LeaveGameRequest,
    SendGameInviteRequest,
    DeclineGameInviteRequest,
    StartGameRequest,
    GetGameHeaderRequest,
    GetGameStateRequest,
    SendRollDiceRequest,
    MovePieceRequest,
    ;
}
