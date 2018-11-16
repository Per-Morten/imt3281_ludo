package no.ntnu.imt3281.ludo.server;

import no.ntnu.imt3281.ludo.api.Error;
import no.ntnu.imt3281.ludo.api.FieldNames;
import no.ntnu.imt3281.ludo.api.JSONValidator;
import no.ntnu.imt3281.ludo.api.RequestType;
import no.ntnu.imt3281.ludo.common.Logger;
import no.ntnu.imt3281.ludo.common.MessageUtility;
import org.json.JSONArray;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;

/**
 * Responsible for responding to user related messages
 */
public class UserManager {
    Database mDB;

    UserManager(Database db) {
        mDB = db;
    }

    public void createUser(RequestType ignored, JSONArray requests, JSONArray successes, JSONArray errors) {
        for (int i = 0; i < requests.length(); i++) {
            var request = requests.getJSONObject(i);
            var requestID = request.getInt(FieldNames.ID);
            var username = request.getString(FieldNames.USERNAME);
            var email = request.getString(FieldNames.EMAIL);

            var password = request.getString(FieldNames.PASSWORD);

            // Think: Really want to create the generalized exception.
            try {
                var salt = randomGenerateSalt();
                var hashedPWD = hashPassword(password, salt);
                var userID = mDB.createUser(username, email, hashedPWD, salt);
                var success = new JSONObject();

                success.put(FieldNames.ID, requestID);
                success.put(FieldNames.USER_ID, userID);
                successes.put(success);

            } catch (SQLException e) {
                Logger.logException(Logger.Level.WARN, e, "Unexpected SQL Exception when trying to create user");
            } catch (NotUniqueValueException e) {
                if (e.getValueName().equals(Database.UserFields.Email)) {
                    MessageUtility.addErrorToArray(errors, requestID, Error.NOT_UNIQUE_EMAIL);

                } else if (e.getValueName().equals(Database.UserFields.Username)) {
                    MessageUtility.addErrorToArray(errors, requestID, Error.NOT_UNIQUE_USERNAME);
                }
            }
        }
    }

    public void deleteUser(RequestType ignored, JSONArray requests, JSONArray successes, JSONArray errors) {
        Logger.log(Logger.Level.DEBUG, "Delete users request received");
    }

    public void updateUser(RequestType ignored, JSONArray requests, JSONArray successes, JSONArray errors) {
        Logger.log(Logger.Level.DEBUG, "Update users request received");
    }

    public void getUser(RequestType ignored, JSONArray requests, JSONArray successes, JSONArray errors) {
        Logger.log(Logger.Level.DEBUG, "Get users request received");
    }

    public void getUserRange(RequestType ignored, JSONArray requests, JSONArray successes, JSONArray errors) {
        Logger.log(Logger.Level.DEBUG, "Get user range request received");
    }

    public void logInUser(RequestType ignored, JSONArray requests, JSONArray successes, JSONArray errors) {
        var request = requests.getJSONObject(0);
        var email = request.getString(FieldNames.EMAIL);
        var requestID = request.getInt(FieldNames.ID);

        User user;
        try {
            user = mDB.getExtendedInformationByEmail(email);

            // This will only return null if the user cannot be found.
            if (user == null) {
                MessageUtility.addErrorToArray(errors, requestID, Error.INVALID_USERNAME_OR_PASSWORD);
                return;
            }

            var requestPassword = request.getString(FieldNames.PASSWORD);
            if (!hashPassword(requestPassword, user.salt).equals(user.password)) {
                MessageUtility.addErrorToArray(errors, requestID, Error.INVALID_USERNAME_OR_PASSWORD);
                return;
            }

            // TODO: If an exception is thrown from the database because the token isn't valid we need to retry!
            var token = generateToken();
            mDB.setUserToken(user.id, token);

            var success = new JSONObject();
            success.put(FieldNames.ID, requestID);
            success.put(FieldNames.USER_ID, user.id);
            success.put(FieldNames.AUTH_TOKEN, token);
            successes.put(success);

        } catch (SQLException e) {
            Logger.logException(Logger.Level.WARN, e, "Unexpected SQL Exception when trying to log in user");
        }
    }

    public void logOutUser(RequestType ignored, JSONArray requests, JSONArray successes, JSONArray errors) {
        for (int i = 0; i < requests.length(); i++) {
            var request = requests.getJSONObject(i);
            var requestID = request.getInt(FieldNames.ID);
            var userID = request.getInt(FieldNames.USER_ID);
            try {
                mDB.setUserToken(userID, null);
            } catch(SQLException e) {
                Logger.logException(Logger.Level.WARN, e, "Unexpected SQL Exception when trying to log in user");
            }
            var success = new JSONObject();
            success.put(FieldNames.ID, requestID);
            successes.put(success);
        }
    }

    public boolean isUserAuthorized(JSONObject request, String token) {
        if (JSONValidator.hasInt(FieldNames.USER_ID, request)) {
            try {
                var id = request.getInt(FieldNames.USER_ID);
                var user = mDB.getUserByToken(token);
                return user != null && user.id == id;
            } catch (SQLException e) {
                Logger.logException(Logger.Level.WARN, e, "Unexpected SQL Exception when trying to getUserByToken");
                return false;
            }
        }

        return true;
    }

    private String randomGenerateSalt() {
        var random = new java.security.SecureRandom();
        var bytes = new byte[32];
        random.nextBytes(bytes);
        return new String(bytes);
    }

    private String generateToken() {
        var random = new java.security.SecureRandom();

        var builder = new StringBuilder();
        // Restricting the values here a bit because we got issues when we put "illegal" characters such as \n into json.
        // However, this should still provide enough place for randomness.
        random.ints(64, 32, 126).forEach(i -> builder.append((char)i));

        return builder.toString();
    }

    private String hashPassword(String password, String salt) {
        try {
            var digest = MessageDigest.getInstance("SHA-256");
            digest.reset();
            digest.update(salt.getBytes(StandardCharsets.UTF_8));
            return new String(digest.digest(password.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            Logger.logException(Logger.Level.ERROR, e, "Platform doesn't support hashing algorithm, cannot run here!");
            return null;
        }
    }

}
