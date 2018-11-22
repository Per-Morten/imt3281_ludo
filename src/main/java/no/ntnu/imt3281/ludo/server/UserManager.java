package no.ntnu.imt3281.ludo.server;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Queue;
import java.util.regex.Pattern;

import no.ntnu.imt3281.ludo.api.*;
import no.ntnu.imt3281.ludo.api.Error;
import org.json.JSONArray;
import org.json.JSONObject;

import no.ntnu.imt3281.ludo.common.Logger;

/**
 * Responsible for parsing and executing the incoming requests related to users & friends.
 * Note: Class is not thread-safe as it communicates with the Database, which is not thread safe.
 * Note: Neither validation nor authorization is done in this class,
 * it expects that all incoming requests have been authorized,
 * and that all are valid JSON objects according to the API specification.
 */
public class UserManager {
    private Database mDB;

    /**
     * Creates a new UserManager with the given Database.
     *
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
     * @param requests  JSONArray containing all the create user requests to execute.
     * @param successes JSONArray where the successes should be appended to.
     * @param errors    JSONArray where the errors should be appended to.
     */
    public void createUser(JSONArray requests, JSONArray successes, JSONArray errors, Queue<Message> events) {
        MessageUtility.each(requests, (requestID, request) -> {

            var username = request.getString(FieldNames.USERNAME);
            var email = request.getString(FieldNames.EMAIL);

            // Check username
            if (!validateUsername(username)) {
                Logger.log(Logger.Level.DEBUG, "User name failed: %s", request);
                MessageUtility.appendError(errors, requestID, Error.MALFORMED_USERNAME);
                return;
            }
            // Check email
            if (!validateEmail(email)) {
                Logger.log(Logger.Level.DEBUG, "email failed: %s", request);
                MessageUtility.appendError(errors, requestID, Error.MALFORMED_EMAIL);
                return;
            }

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
     * @param requests  JSONArray containing all the delete user requests to execute.
     * @param successes JSONArray where the successes should be appended to.
     * @param errors    JSONArray where the errors should be appended to.
     */
    public void deleteUser(JSONArray requests, JSONArray successes, JSONArray errors, Queue<Message> events) {
        MessageUtility.each(requests, (requestID, request) -> {
            var userID = request.getInt(FieldNames.USER_ID);
            try {
                mDB.deleteUser(userID);
                MessageUtility.appendSuccess(successes, requestID, new JSONObject());
            } catch (SQLException e) {
                Logger.logException(Logger.Level.WARN, e, "Unexpected SQL Exception when trying to log delete user");
            }
        });
    }

    /**
     * Parses and executes the given update user requests.
     * Successes are appended to the success array.
     * Errors are appended to the error array.
     *
     * @param requests  JSONArray containing all the update user requests to execute.
     * @param successes JSONArray where the successes should be appended to.
     * @param errors    JSONArray where the errors should be appended to.
     */
    public void updateUser(JSONArray requests, JSONArray successes, JSONArray errors, Queue<Message> events) {
        MessageUtility.each(requests, (requestID, request) -> {

            if (!validateUsername(request.getString(FieldNames.USERNAME))) {
                Logger.log(Logger.Level.DEBUG, "User name failed: %s", request);
                MessageUtility.appendError(errors, requestID, Error.MALFORMED_USERNAME);
                return;
            }
            // Check email
            if (!validateEmail(request.getString(FieldNames.EMAIL))) {
                Logger.log(Logger.Level.DEBUG, "email failed: %s", request);
                MessageUtility.appendError(errors, requestID, Error.MALFORMED_EMAIL);
                return;
            }
            // Check avatar uri
            String uri = request.getString(FieldNames.AVATAR_URI);
            if (uri != "" && !validateAvatarURI(uri)) {
                Logger.log(Logger.Level.DEBUG, "avatar uri failed: %s", request);
                MessageUtility.appendError(errors, requestID, Error.MALFORMED_AVATAR_URI);
                return;
            }
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
                Logger.logException(Logger.Level.WARN, e, "Unexpected SQL Exception when trying to update user");
            } catch (APIErrorException e) {
                MessageUtility.appendError(errors, requestID, e.getError());
            }
        });
    }

    /**
     * Checks if username is between 5 and 50 symbols, and only contains letters
     * (both uppercase and lowercase), numbers, dashes and underscores.
     *
     * @param name
     * @return
     */
    public boolean validateUsername(String name) {
        return Pattern.matches("^(\\w|-|_){4,50}$", name);
    }

    /**
     * Checks if email is a valid email address. Regex found at
     * https://stackoverflow.com/questions/8204680/java-regex-email#8204716 and
     * modified to accept lowercase letters.
     *
     * @param email
     * @return
     */
    public boolean validateEmail(String email) {
        return Pattern.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$", email);
    }

    /**
     * Checks if avatar URI is a valid URL. Regex found at
     * https://stackoverflow.com/questions/31440758/perfect-url-validation-regex-in-java
     *
     * @param URI
     * @return
     */
    public boolean validateAvatarURI(String URI) {
        return Pattern.matches(
                "(?i)^(?:(?:https?|ftp)://)(?:\\S+(?::\\S*)?@)?(?:(?!(?:10|127)(?:\\.\\d{1,3}){3})(?!(?:169\\.254|192\\.168)(?:\\.\\d{1,3}){2})(?!172\\.(?:1[6-9]|2\\d|3[0-1])(?:\\.\\d{1,3}){2})(?:[1-9]\\d?|1\\d\\d|2[01]\\d|22[0-3])(?:\\.(?:1?\\d{1,2}|2[0-4]\\d|25[0-5])){2}(?:\\.(?:[1-9]\\d?|1\\d\\d|2[0-4]\\d|25[0-4]))|(?:(?:[a-z\\u00a1-\\uffff0-9]-*)*[a-z\\u00a1-\\uffff0-9]+)(?:\\.(?:[a-z\\u00a1-\\uffff0-9]-*)*[a-z\\u00a1-\\uffff0-9]+)*(?:\\.(?:[a-z\\u00a1-\\uffff]{2,}))\\.?)(?::\\d{2,5})?(?:[/?#]\\S*)?$",
                URI);
    }

    /**
     * Parses and executes the given update get user requests.
     * Successes are appended to the success array.
     * Errors are appended to the error array.
     *
     * @param requests  JSONArray containing all the get user requests to execute.
     * @param successes JSONArray where the successes should be appended to.
     * @param errors    JSONArray where the errors should be appended to.
     */
    public void getUser(JSONArray requests, JSONArray successes, JSONArray errors, Queue<Message> events) {
        MessageUtility.each(requests, (requestID, request) -> {
            var userID = request.getInt(FieldNames.USER_ID);
            try {
                var user = mDB.getUserByID(userID);
                var json = userToJSON(user);
                MessageUtility.appendSuccess(successes, requestID, json);
            } catch (SQLException e) {
                Logger.logException(Logger.Level.WARN, e, "Unexpected SQL Exception when trying to get user");
            }
        });
    }

    /**
     * Parses and executes the given get user range requests.
     * Successes are appended to the success array.
     * Errors are appended to the error array.
     *
     * @param requests  JSONArray containing all the get user range requests to execute.
     * @param successes JSONArray where the successes should be appended to.
     * @param errors    JSONArray where the errors should be appended to.
     */
    public void getUserRange(JSONArray requests, JSONArray successes, JSONArray errors, Queue<Message> events) {
        MessageUtility.each(requests, (requestID, request) -> {
            var pageIdx = request.getInt(FieldNames.PAGE_INDEX);
            try {
                var users = mDB.getUserRange(pageIdx);
                var usersArray = new JSONArray();

                for (var user : users) {
                    usersArray.put(userToJSON(user));
                }

                var success = new JSONObject();
                success.put(FieldNames.RANGE, usersArray);
                MessageUtility.appendSuccess(successes, requestID, success);

            } catch (SQLException e) {
                Logger.logException(Logger.Level.WARN, e, "Unexpected SQL Exception when trying to get user range");
            }
        });
    }

    /**
     * Parses and executes the given login request.
     * Successes are appended to the success array.
     * Errors are appended to the error array.
     * Note: Due to how we need the socketIDs for the SocketManager to be mapped to userIDs,
     * we do not accept multiple logins, we will simply take the first in the request.
     *
     * @param requests  JSONArray containing all the log in request to execute.
     * @param successes JSONArray where the successes should be appended to.
     * @param errors    JSONArray where the errors should be appended to.
     */
    public void logInUser(JSONArray requests, JSONArray successes, JSONArray errors, Queue<Message> events) {
        var request = requests.getJSONObject(0);
        var email = request.getString(FieldNames.EMAIL);
        var requestID = request.getInt(FieldNames.ID);

        Database.User user;
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
                } catch (NotUniqueTokenException e) {
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
     * @param requests  JSONArray containing all the get user range requests to execute.
     * @param successes JSONArray where the successes should be appended to.
     * @param errors    JSONArray where the errors should be appended to.
     */
    public void logOutUser(JSONArray requests, JSONArray successes, JSONArray errors, Queue<Message> events) {
        MessageUtility.each(requests, (requestID, request) -> {
            var userID = request.getInt(FieldNames.USER_ID);
            try {
                mDB.setUserToken(userID, null);
                MessageUtility.appendSuccess(successes, requestID, new JSONObject());
            } catch (SQLException e) {
                Logger.logException(Logger.Level.WARN, e, "Unexpected SQL Exception when trying to log out user");
            }
        });
    }

    /**
     * Parses and executes the given friend requests.
     * When a friend request is initiated, the relationship is given a pending value (as it has not been confirmed by someone else yet)
     * When a pending friend request is accepted (by the other person) the relationship is given the value of friended.
     * Both when a friend request is initiated, and when it is accepted both the users will be notified that they should update their friend lists.
     *
     * @param requests      JSONArray containing the friend requests
     * @param successes     JSONArray where the successes should be appended.
     * @param errors        JSONArray where the errors should be appended.
     */
    public void friend(JSONArray requests, JSONArray successes, JSONArray errors, Queue<Message> events) {
        MessageUtility.each(requests, (requestID, request) -> {

            var userID = request.getInt(FieldNames.USER_ID);
            var friendID = request.getInt(FieldNames.OTHER_ID);

            try {
                var currentRelationship = mDB.getRelationship(userID, friendID);

                // Either we didn't know each other from before, or we have removed each other as friends (or unignored, which is same as unfriend)
                if (currentRelationship == null) {
                    mDB.createRelationship(userID, friendID, FriendStatus.PENDING);
                    mDB.createRelationship(friendID, userID, FriendStatus.PENDING);

                    events.add(new Message(MessageUtility.createEvent(EventType.FRIEND_UPDATE), List.of(userID, friendID)));

                    // We have removed each other as friends (or unignored, which is same as unfriend)
                } else if (Arrays.stream(currentRelationship).allMatch(value -> value.status == FriendStatus.UNFRIENDED)) {
                    mDB.setRelationshipStatus(userID, friendID, FriendStatus.PENDING);
                    mDB.setRelationshipStatus(friendID, userID, FriendStatus.PENDING);

                    // We have a relationship from before, and I (userID) am not the one who initiated it.
                } else if (currentRelationship[0].rowID > currentRelationship[1].rowID &&
                        Arrays.stream(currentRelationship).allMatch(value -> value.status == FriendStatus.PENDING)) {

                    mDB.setRelationshipStatus(userID, friendID, FriendStatus.FRIENDED);
                    mDB.setRelationshipStatus(friendID, userID, FriendStatus.FRIENDED);

                    events.add(new Message(MessageUtility.createEvent(EventType.FRIEND_UPDATE), List.of(userID, friendID)));
                }  // The last case: One of us is ignoring the other!

                MessageUtility.appendSuccess(successes, requestID, new JSONObject());
            } catch (SQLException e) {
                Logger.logException(Logger.Level.WARN, e, "Unexpected SQL Exception when trying to friend user");
            }

        });
    }

    /**
     * Parses and executes the given unfriend requests.
     * Unfriend requests on people who have never been friends does not create a relationship between two users.
     * In the case where a user is ignored, trying to unfriend the user that has ignored them will not clear the
     * "ignored" value from the other users perspective.
     * However, in the case where a user unfriends someone they have ignored, their "view" of the relationship will
     * change to unfriended, allowing the newly unfriended user to make a friend request should they so desired.
     *
     * @param requests JSONArray containing the unfriend requests
     * @param successes JSONArray where the successes should be appended.
     * @param errors JSONArray where the errors should be appended.
     */
    public void unfriend(JSONArray requests, JSONArray successes, JSONArray errors, Queue<Message> events) {
        MessageUtility.each(requests, (requestID, request) -> {
            var userID = request.getInt(FieldNames.USER_ID);
            var friendID = request.getInt(FieldNames.OTHER_ID);

            try {
                var currentRelationship = mDB.getRelationship(userID, friendID);

                // if ignored by the other, mark you as unfriended, the other should still be ignored
                // if both is pending, mark both as unfriended.
                // if both is accepted, mark both as unfriended.
                if (currentRelationship != null) {
                    for (var relation : currentRelationship) {
                        if (relation.status == FriendStatus.PENDING || relation.status == FriendStatus.FRIENDED) {
                            mDB.setRelationshipStatus(relation.userID, relation.friendID, FriendStatus.UNFRIENDED);
                        }
                    }

                    if (currentRelationship[0].status == FriendStatus.IGNORED) {
                        mDB.setRelationshipStatus(userID, friendID, FriendStatus.UNFRIENDED);
                    }
                }

                MessageUtility.appendSuccess(successes, requestID, new JSONObject());
            } catch (SQLException e) {
                Logger.logException(Logger.Level.WARN, e, "Unexpected SQL Exception when trying to unfriend user");
            }

        });
    }

    /**
     * Parses and executes the given ignore requests.
     * Unless two users are friends (then one party must unfriend first) ignore request always succeeds (given valid user and friend id's are supplied),
     * even in the case where two users have never had a relationship with each other a user can ignore another one.
     *
     * @param requests JSONArray containing the ignore requests
     * @param successes JSONArray where the successes should be appended.
     * @param errors JSONArray where the errors should be appended.
     */
    public void ignore(JSONArray requests, JSONArray successes, JSONArray errors, Queue<Message> events) {
        MessageUtility.each(requests, (requestID, request) -> {
            var userID = request.getInt(FieldNames.USER_ID);
            var friendID = request.getInt(FieldNames.OTHER_ID);

            try {
                var currentRelationship = mDB.getRelationship(userID, friendID);
                if (currentRelationship != null) {
                    if (Arrays.stream(currentRelationship).anyMatch(value -> value.status == FriendStatus.FRIENDED)) {
                        MessageUtility.appendError(errors, requestID, Error.USER_IS_FRIEND);
                        return;
                    }

                    mDB.setRelationshipStatus(userID, friendID, FriendStatus.IGNORED);
                }

                if (currentRelationship == null) {
                    mDB.createRelationship(userID, friendID, FriendStatus.IGNORED);
                    mDB.createRelationship(friendID, userID, FriendStatus.UNFRIENDED);
                }

                MessageUtility.appendSuccess(successes, requestID, new JSONObject());

            } catch (SQLException e) {
                Logger.logException(Logger.Level.WARN, e, "Unexpected SQL Exception when trying to ignore user");
            }
        });
    }

    /**
     * Parses and executes the given get friend range requests.
     * Successes are appended to the success array.
     * Errors are appended to the error array.
     *
     * @param requests  JSONArray containing all the get friend range requests to execute.
     * @param successes JSONArray where the successes should be appended to.
     * @param errors    JSONArray where the errors should be appended to.
     */
    public void getFriendRange(JSONArray requests, JSONArray successes, JSONArray errors, Queue<Message> events) {
        MessageUtility.each(requests, (requestID, request) -> {
            var userID = request.getInt(FieldNames.USER_ID);
            var pageIdx = request.getInt(FieldNames.PAGE_INDEX);
            try {
                var friends = mDB.getFriendsRange(userID, pageIdx);
                var friendsArray = new JSONArray();

                for (var friend : friends) {
                    friendsArray.put(friendToJSON(friend));
                }

                var success = new JSONObject();
                success.put(FieldNames.RANGE, friendsArray);
                MessageUtility.appendSuccess(successes, requestID, success);

            } catch (SQLException e) {
                Logger.logException(Logger.Level.WARN, e, "Unexpected SQL Exception when trying to get friend range");
            }
        });
    }

    /**
     * Checks if the user with the specified token is authorized to execute the request
     * stored in request.
     *
     * @param request The request to check if user is authorized to execute.
     * @param token   The token belonging to the user trying to execute the request.
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

        return false;
    }

    /**
     * Applied the first order filter in the requests within requests, removes the erroneous ones and
     * appends the errors to the errors array.
     * This filter is just a basic filter to get rid of the following common errors:
     * * Trying to access a user (user_id or other_id) that does not exist.
     * * Trying to have a relationship with yourself
     *
     * @param type The type of request that are stored in requests.
     * @param requests The actual requests themselves
     * @param errors The JSONArray to put the errors in.
     */
    public void applyFirstOrderFilter(RequestType type, JSONArray requests, JSONArray errors) {
        MessageUtility.applyFilter(requests, (id, request) -> {

            if (JSONValidator.hasInt(FieldNames.USER_ID, request) && !userExists(request.getInt(FieldNames.USER_ID))) {
                MessageUtility.appendError(errors, id, Error.USER_ID_NOT_FOUND);
                return false;
            }

            if (JSONValidator.hasInt(FieldNames.OTHER_ID, request) && !userExists(request.getInt(FieldNames.OTHER_ID))) {
                MessageUtility.appendError(errors, id, Error.OTHER_ID_NOT_FOUND);
                return false;
            }

            if (type == RequestType.UNFRIEND_REQUEST || type == RequestType.IGNORE_REQUEST || type == RequestType.FRIEND_REQUEST) {
                if (request.getInt(FieldNames.USER_ID) == request.getInt(FieldNames.OTHER_ID)) {
                    MessageUtility.appendError(errors, id, Error.USER_AND_OTHER_ID_IS_SAME);
                    return false;
                }
            }

            return true;
        });
    }

    public boolean tokenExists(String token) {
        try {
            var user = mDB.getUserByToken(token);
            return user != null;
        } catch (SQLException e) {
            Logger.logException(Logger.Level.WARN, e, "Unexpected SQL Exception when trying to getUserByToken");
            return false;
        }
    }

    public boolean areUsersFriends(int userID, int otherID) {
        try {
            var relationship = mDB.getRelationship(userID, otherID);
            return relationship != null && Arrays.stream(relationship).allMatch(item -> item.status == FriendStatus.FRIENDED);
        } catch (SQLException e) {
            Logger.logException(Logger.Level.WARN, e, "Exception encountered when checking if users are friends");
        }
        return false;
    }

    public boolean userExists(int userID) {
        try {
            return mDB.getUserByID(userID) != null;
        } catch (SQLException e) {
            Logger.logException(Logger.Level.WARN, e, "Exception encountered when checking if user exists");
        }
        return false;
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

    private JSONObject userToJSON(Database.User user) {
        var json = new JSONObject();
        json.put(FieldNames.USER_ID, user.id);
        json.put(FieldNames.USERNAME, user.username);
        json.put(FieldNames.AVATAR_URI, (user.avatarURI != null) ? user.avatarURI : "");

        return json;
    }

    private JSONObject friendToJSON(Database.Friend friend) {
        var json = new JSONObject();
        json.put(FieldNames.USER_ID, friend.userID);
        json.put(FieldNames.USERNAME, friend.username);
        json.put(FieldNames.STATUS, friend.status.toInt());

        return json;
    }

}
