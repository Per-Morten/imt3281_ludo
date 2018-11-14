package no.ntnu.imt3281.ludo.api;

public class FieldNames {
    // Protocol Related
   public static final String ID = "id";
   public static final String PAYLOAD = "payload";
   public static final String AUTH_TOKEN = "auth_token";
   public static final String CODE = "code";
   public static final String TYPE = "type";
   public static final String SUCCESS = "success";
   public static final String ERROR = "error";

   // User Related
   public static final String USER = "user";
   public static final String USER_ID = "user_id";
   public static final String USERNAME = "username";
   public static final String EMAIL = "email";
   public static final String PASSWORD = "password";
   public static final String PAGE_INDEX = "page_index";
   public static final String AVATAR_URI = "avatar_uri";

   // Friend Related
   public static final String FRIEND = "friend";
   public static final String OTHER_ID = "other_id";

   // Chat related
   public static final String NAME = "name";
   public static final String CHAT_ID = "chat_id";
   public static final String PARTICIPANT_ID = "participant_id";
   public static final String MESSAGE = "message";

   // Game Related
   public static final String GAME_ID = "game_id";
   public static final String OWNER_ID = "owner_id";
   public static final String PLAYER_ID = "player_id";
   public static final String PENDING_ID = "pending_id";

   // Game Cache Related
   public static final String PLAYER_ORDER = "player_order";
   public static final String CURRENT_PLAYER_ID = "current_player_id";
   public static final String NEXT_ACTION = "next_action";
   public static final String PREVIOUS_DICE_THROW = "previous_dice_throw";
   public static final String PIECE_POSITIONS = "piece_positions";
   public static final String PIECE_INDEX = "piece_index";

   // Invite Related
   public static final String INVITEE_ID = "invitee_id";
}
