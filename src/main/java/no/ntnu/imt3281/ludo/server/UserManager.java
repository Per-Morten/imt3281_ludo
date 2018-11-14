package no.ntnu.imt3281.ludo.server;

import java.sql.SQLException;

import no.ntnu.imt3281.ludo.api.FieldNames;
import org.json.JSONArray;
import org.json.JSONObject;

import no.ntnu.imt3281.ludo.api.Error;
import no.ntnu.imt3281.ludo.api.RequestType;
import no.ntnu.imt3281.ludo.common.Logger;


/**
 * Responsible for responding to user related messages
 */
public class UserManager {
    Database mDB;

    UserManager(Database db) {
        mDB = db;
    }

    public void createUsers(RequestType ignored, JSONArray requests, JSONArray successes, JSONArray errors) {
        for (int i = 0; i < requests.length(); i++) {
            var request = requests.getJSONObject(i);
            var requestID = request.getInt(FieldNames.ID);
            var username = request.getString(FieldNames.USERNAME);
            var email = request.getString(FieldNames.EMAIL);

            // TODO: This should be hashed before being stored in the database.
            var password = request.getString(FieldNames.PASSWORD);

            // Think: Really want to create the generalized exception.
            try {
                var userID = mDB.createUser(username, email, password);
                var success = new JSONObject();
                success.put(FieldNames.ID, requestID);
                success.put(FieldNames.USER_ID, userID);
                successes.put(success);
            } catch (SQLException e) {
                Logger.logException(Logger.Level.WARN, e,
                        "Unexpected SQL Exception when trying to create user");
            } catch (NotUniqueValueException e) {
                var error = new JSONObject();
                error.put(FieldNames.ID, requestID);
                var codes = new JSONArray();
                if (e.getValueName().equals(Database.UserFields.Email)) {
                    codes.put(Error.NOT_UNIQUE_EMAIL);
                } else if (e.getValueName().equals(Database.UserFields.Username)) {
                    codes.put(Error.NOT_UNIQUE_USERNAME);
                }

                error.put(FieldNames.CODE, codes);
                errors.put(error);
            }
        }
    }

    public void deleteUsers(RequestType ignored, JSONArray requests, JSONArray successes, JSONArray errors) {
        Logger.log(Logger.Level.DEBUG, "Delete users request received");
    }

    public void updateUsers(RequestType ignored, JSONArray requests, JSONArray successes, JSONArray errors) {
        Logger.log(Logger.Level.DEBUG, "Update users request received");
    }

    public void getUsers(RequestType ignored, JSONArray requests, JSONArray successes, JSONArray errors) {
        Logger.log(Logger.Level.DEBUG, "Get users request received");
    }

    public void logInUsers(RequestType ignored, JSONArray requests, JSONArray successes, JSONArray errors) {
        Logger.log(Logger.Level.DEBUG, "Login users request received");
    }

    public void logOutUsers(RequestType ignored, JSONArray requests, JSONArray successes, JSONArray errors) {
        Logger.log(Logger.Level.DEBUG, "Logout users request received");
    }
}
