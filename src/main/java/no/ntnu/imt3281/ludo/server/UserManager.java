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
 * Responsible for parsing and executing the incoming requests related to users & friends.
 * Note: Class is not thread-safe as it communicates with the Database, which is not thread safe.
 * Note: Neither validation nor authorization is done in this class,
 *       it expects that all incoming requests have been authorized,
 *       and that all are valid JSON objects according to the API specification.
 */
public class UserManager {
    private Database mDB;

    /**
     * Creates a new UserManager with the given Database.
     * @param db the database the UserManager should use.
     */
    UserManager(Database db) {
        mDB = db;
    }

    /**
     * Parses and executes the given create user requests.
     * Successes are appended to the success array.
     * Errors are appended to the error array.
     *
     * @param ignored
     * @param requests  JSONArray containing all the create user requests to execute.
     * @param successes JSONArray where the successes should be appended to.
     * @param errors    JSONArray where the errors should be appended to.
     */
    public void createUser(RequestType ignored, JSONArray requests, JSONArray successes, JSONArray errors) {
        MessageUtility.each(requests, (requestID, request) -> {
            var username = request.getString(FieldNames.USERNAME);
            var email = request.getString(FieldNames.EMAIL);

            var password = request.getString(FieldNames.PASSWORD);

            try {
                var salt = randomGenerateSalt();
                var hashedPWD = hashPassword(password, salt);
                var userID = mDB.createUser(username, email, hashedPWD, salt);
                var success = new JSONObject();
                success.put(FieldNames.USER_ID, userID);
                MessageUtility.appendSuccess(successes, requestID, success);

            } catch (SQLException e) {
                Logger.logException(Logger.Level.WARN, e, "Unexpected SQL Exception when trying to create user");
            } catch (APIErrorException e) {
                MessageUtility.appendError(errors, requestID, e.getError());
            }
        });
    }

    /**
     * Parses and executes the given delete user requests.
     * Successes are appended to the success array.
     * Errors are appended to the error array.
     *
     * @param ignored
     * @param requests  JSONArray containing all the delete user requests to execute.
     * @param successes JSONArray where the successes should be appended to.
     * @param errors    JSONArray where the errors should be appended to.
     */
    public void deleteUser(RequestType ignored, JSONArray requests, JSONArray successes, JSONArray errors) {
        MessageUtility.each(requests, (requestID, request) -> {
            var userID = request.getInt(FieldNames.USER_ID);
            try {
                mDB.deleteUser(userID);
                MessageUtility.appendSuccess(successes, requestID, new JSONObject());
            } catch (SQLException e) {
                Logger.logException(Logger.Level.WARN, e, "Unexpected SQL Exception when trying to log in user");
            }
        });
    }

    /**
     * Parses and executes the given update user requests.
     * Successes are appended to the success array.
     * Errors are appended to the error array.
     *
     * @param ignored
     * @param requests  JSONArray containing all the update user requests to execute.
     * @param successes JSONArray where the successes should be appended to.
     * @param errors    JSONArray where the errors should be appended to.
     */
    public void updateUser(RequestType ignored, JSONArray requests, JSONArray successes, JSONArray errors) {
        MessageUtility.each(requests, (requestID, request) -> {
            try {
                var salt = randomGenerateSalt();
                mDB.updateUser(request.getInt(FieldNames.USER_ID),
                        request.getString(FieldNames.USERNAME),
                        request.getString(FieldNames.EMAIL),
                        hashPassword(request.getString(FieldNames.PASSWORD), salt),
                        request.getString(FieldNames.AVATAR_URI),
                        salt);

                MessageUtility.appendSuccess(successes, requestID, new JSONObject());
            } catch (SQLException e) {
                Logger.logException(Logger.Level.WARN, e, "Unexpected SQL Exception when trying to log in user");
            } catch (APIErrorException e) {
                MessageUtility.appendError(errors, requestID, e.getError());
            }
        });
    }

    /**
     * Parses and executes the given update get user requests.
     * Successes are appended to the success array.
     * Errors are appended to the error array.
     *
     * @param ignored
     * @param requests  JSONArray containing all the get user requests to execute.
     * @param successes JSONArray where the successes should be appended to.
     * @param errors    JSONArray where the errors should be appended to.
     */
    public void getUser(RequestType ignored, JSONArray requests, JSONArray successes, JSONArray errors) {
        MessageUtility.each(requests, (requestID, request) -> {
            var userID = request.getInt(FieldNames.USER_ID);
            try {
                var user = mDB.getUserByID(userID);
                if (user != null) {
                    var json = userToJSON(user);
                    MessageUtility.appendSuccess(successes, requestID, json);
                } else {
                    MessageUtility.appendError(errors, requestID, Error.USER_ID_NOT_FOUND);
                }
            } catch (SQLException e) {
                Logger.logException(Logger.Level.WARN, e, "Unexpected SQL Exception when trying to log in user");
            }
        });
    }

    /**
     * Parses and executes the given update get user range requests.
     * Successes are appended to the success array.
     * Errors are appended to the error array.
     *
     * @param ignored
     * @param requests  JSONArray containing all the get user range requests to execute.
     * @param successes JSONArray where the successes should be appended to.
     * @param errors    JSONArray where the errors should be appended to.
     */
    public void getUserRange(RequestType ignored, JSONArray requests, JSONArray successes, JSONArray errors) {
        MessageUtility.each(requests, (requestID, request) -> {
            var pageIdx = request.getInt(FieldNames.PAGE_INDEX);
            try {
                var users = mDB.getUserPage(pageIdx);
                var usersArray = new JSONArray();

                for (var user : users) {
                    usersArray.put(userToJSON(user));
                }

                var success = new JSONObject();
                success.put(FieldNames.RANGE, usersArray);
                MessageUtility.appendSuccess(successes, requestID, success);

            } catch (SQLException e) {
                Logger.logException(Logger.Level.WARN, e, "Unexpected SQL Exception when trying to log in user");
            }
        });
    }

    /**
     * Parses and executes the given login request.
     * Successes are appended to the success array.
     * Errors are appended to the error array.
     * Note: Due to how we need the socketIDs for the SocketManager to be mapped to userIDs,
     *       we do not accept multiple logins, we will simply take the first in the request.
     *
     * @param ignored
     * @param requests  JSONArray containing all the log in request to execute.
     * @param successes JSONArray where the successes should be appended to.
     * @param errors    JSONArray where the errors should be appended to.
     */
    public void logInUser(RequestType ignored, JSONArray requests, JSONArray successes, JSONArray errors) {
        var request = requests.getJSONObject(0);
        var email = request.getString(FieldNames.EMAIL);
        var requestID = request.getInt(FieldNames.ID);

        User user;
        try {
            user = mDB.getExtendedInformationByEmail(email);

            // This will only return null if the user cannot be found.
            if (user == null) {
                MessageUtility.appendError(errors, requestID, Error.INVALID_USERNAME_OR_PASSWORD);
                return;
            }

            var requestPassword = request.getString(FieldNames.PASSWORD);
            if (!hashPassword(requestPassword, user.salt).equals(user.password)) {
                MessageUtility.appendError(errors, requestID, Error.INVALID_USERNAME_OR_PASSWORD);
                return;
            }
            
            String token = null;
            boolean isValidToken = false;
            while (!isValidToken) {
                token = generateToken();
                try {
                    mDB.setUserToken(user.id, token);
                    isValidToken = true;
                } catch(NotUniqueTokenException e) {
                    Logger.log(Logger.Level.WARN, "Generated a token that was not unique! Retrying");
                }
            }

            var success = new JSONObject();
            success.put(FieldNames.USER_ID, user.id);
            success.put(FieldNames.AUTH_TOKEN, token);
            MessageUtility.appendSuccess(successes, requestID, success);

        } catch (SQLException e) {
            Logger.logException(Logger.Level.WARN, e, "Unexpected SQL Exception when trying to log in user");
        }
    }

    /**
     * Parses and executes the given logout user requests.
     * Successes are appended to the success array.
     * Errors are appended to the error array.
     *
     * @param ignored
     * @param requests  JSONArray containing all the get user range requests to execute.
     * @param successes JSONArray where the successes should be appended to.
     * @param errors    JSONArray where the errors should be appended to.
     */
    public void logOutUser(RequestType ignored, JSONArray requests, JSONArray successes, JSONArray errors) {
        MessageUtility.each(requests, (requestID, request) -> {
            var userID = request.getInt(FieldNames.USER_ID);
            try {
                mDB.setUserToken(userID, null);
                MessageUtility.appendSuccess(successes, requestID, new JSONObject());
            } catch (SQLException e) {
                Logger.logException(Logger.Level.WARN, e, "Unexpected SQL Exception when trying to log in user");
            }
        });
    }

    /**
     * Checks if the user with the specified token is authorized to execute the request
     * stored in request.
     *
     * @param request The request to check if user is authorized to execute.
     * @param token The token belonging to the user trying to execute the request.
     * @return True if the user is authorized, false otherwise (also in the case where an exception is thrown, but that shouldn't happen).
     */
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
        random.ints(64, 32, 126).forEach(i -> builder.append((char) i));

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

    private JSONObject userToJSON(User user) {
        var json = new JSONObject();
        json.put(FieldNames.USER_ID, user.id);
        json.put(FieldNames.USERNAME, user.username);
        json.put(FieldNames.AVATAR_URI, (user.avatarURI != null) ? user.avatarURI : "");

        return json;
    }
}
