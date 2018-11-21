package no.ntnu.imt3281.ludo.client;

import javafx.scene.image.Image;
import org.json.JSONObject;

import static no.ntnu.imt3281.ludo.api.FieldNames.*;

public class User {
    public int id = -1;
    public String username = "";
    public String avatarURL = "";
    public Image avatar;

    User() {}
    User(JSONObject json) {
        id = json.getInt(USER_ID);
        username = json.getString(USERNAME);
        avatarURL = json.getString(AVATAR_URI);
    }
}
