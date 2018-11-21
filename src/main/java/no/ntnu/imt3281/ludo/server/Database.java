package no.ntnu.imt3281.ludo.server;

import no.ntnu.imt3281.ludo.api.Error;
import no.ntnu.imt3281.ludo.api.FriendStatus;
import no.ntnu.imt3281.ludo.api.GlobalChat;
import no.ntnu.imt3281.ludo.common.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Class working as the interface to a database (In this case SQLite).
 * This class can be accessed concurrently (but should probably not be closed concurrently) as
 * this class does not hold any other members than the connection to the database.
 * Concurrent access to the database is guaranteed as the SQLite DB is run in serialized mode.
 * <p>
 * See: https://www.sqlite.org/threadsafe.html
 */
public class Database implements AutoCloseable {

    private Connection mConnection;

    /**
     * Creating all the fieldnames as static strings to avoid problems with typos,
     * or if I update the names.
     */
    public static class UserFields {
        public static final String DBName = "user";
        public static final String ID = "user_id";
        public static final String Username = "username";
        public static final String Email = "email";
        public static final String Password = "password";
        public static final String AvatarURI = "avatar_uri";
        public static final String Token = "token";
        public static final String Salt = "salt";
    }

    public static class FriendFields {
        public static final String DBName = "friend";
        public static final String ID = "id";
        public static final String UserID = "user_id";
        public static final String FriendID = "friend_id";
        public static final String Status = "status";
    }

    public static class ChatFields {
        public static final String DBName = "chat";
        public static final String ID = "id";
        public static final String Name = "name";
    }

    public static class ChatMessageFields {
        public static final String DBName = "chat_message";
        public static final String ID = "id";
        public static final String ChatID = "chat_id";
        public static final String UserID = "user_id";
        public static final String Message = "message";
        public static final String Timestamp = "time_stamp";
    }

    private static final int UnassignedID = -1;

    private String mDBURL;

    /**
     * Creates or connects to the database specified by the filename passed as
     * parameter. Creates the tables needed upon construction if they don't already
     * exist.
     *
     * @param dbFilename The name of the SQLite database to create/connect to.
     * @throws SQLException Throws SQLException if anything SQL related fails, like
     *                      malformed syntax etc, shouldn't happen.
     */
    public Database(String dbFilename) throws SQLException {
        mDBURL = "jdbc:sqlite:" + dbFilename;
        mConnection = DriverManager.getConnection(mDBURL);
        createUserTable();
        createFriendsTable();
        createChatTable();
        createChatMessagesTable();
    }


    private Connection getOneOffConnection() throws SQLException {
        return mConnection;
    }

    private Connection getMultiStatementConnection() throws SQLException {
        return DriverManager.getConnection(mDBURL);
    }


    /**
     * Closes the connection to the database if it has been opened.
     *
     * @throws SQLException
     */
    @Override
    public void close() throws SQLException {
        if (mConnection != null) {
            mConnection.close();
        }
    }


    ///////////////////////////////////////////////////////
    /// USER RELATED
    ///////////////////////////////////////////////////////

    /**
     * Creates a user with the specified username, email and password.
     *
     * @param username The username of the new user.
     * @param email    The email address of the new user.
     * @param password The password of the new user
     * @return Returns the user_id of the new user
     * @throws SQLException      Throws SQLException Upon SQL Errors (Should
     *                           not happen)
     * @throws APIErrorException Throws APIErrorException in the case
     *                           where a field that is required to be Unique
     *                           isn't unique. @see APIErrorException
     *                           In this case this is if the username or email isn't unique.
     */
    public int createUser(String username, String email, String password, String salt) throws SQLException {
        if (valueAlreadyExists(UnassignedID, UserFields.DBName, UserFields.ID, UserFields.Email, email))
            throw new APIErrorException(Error.NOT_UNIQUE_EMAIL);

        if (valueAlreadyExists(UnassignedID, UserFields.DBName, UserFields.ID, UserFields.Username, username))
            throw new APIErrorException(Error.NOT_UNIQUE_USERNAME);

        try (var statement = getOneOffConnection().prepareStatement(String.format("INSERT INTO %s(%s, %s, %s, %s) VALUES (?, ?, ?, ?)",
                UserFields.DBName, UserFields.Username, UserFields.Email, UserFields.Password, UserFields.Salt))) {
            statement.setString(1, username);
            statement.setString(2, email);
            statement.setString(3, password);
            statement.setString(4, salt);

            statement.execute();
        }

        // Need to get the id so it can be sent back
        try (var query = getOneOffConnection().prepareStatement(
                String.format("SELECT %s FROM %s WHERE %s=?", UserFields.ID, UserFields.DBName, UserFields.Email))) {
            query.setString(1, email);
            var res = query.executeQuery();
            return res.getInt(UserFields.ID);
        }
    }

    /**
     * Updates the user indicated by the id
     *
     * @param id        The id of the user to be updated.
     * @param username  The new username of the user.
     * @param email     The new email of the user.
     * @param password  The new password of the user.
     * @param avatarURI The new avatar URI of the user.
     * @throws SQLException      Throws SQLException on SQL errors (should not
     *                           happen)
     * @throws APIErrorException Throws APIErrorException in the case
     *                           where a field that is required to be Unique
     *                           isn't unique. @see APIErrorException
     *                           In this case this is if the username or email isn't unique.
     */
    public void updateUser(int id, String username, String email, String password, String avatarURI, String salt)
            throws SQLException {
        if (valueAlreadyExists(id, UserFields.DBName, UserFields.ID, UserFields.Email, email))
            throw new APIErrorException(Error.NOT_UNIQUE_EMAIL);

        if (valueAlreadyExists(id, UserFields.DBName, UserFields.ID, UserFields.Username, username))
            throw new APIErrorException(Error.NOT_UNIQUE_USERNAME);

        try (var statement = getOneOffConnection().prepareStatement(String.format(
                "UPDATE %s SET %s = ?, %s = ?, %s = ?, %s = ?, %s = ? WHERE %s=?", UserFields.DBName, UserFields.Username,
                UserFields.Email, UserFields.Password, UserFields.AvatarURI, UserFields.Salt, UserFields.ID))) {


            statement.setString(1, username);
            statement.setString(2, email);
            statement.setString(3, password);
            statement.setString(4, avatarURI);
            statement.setString(5, salt);
            statement.setInt(6, id);
            statement.execute();
        }
    }

    /**
     * Finds the user with the specified user_id.
     *
     * @param id The user_id of the user to look for.
     * @return A user structure containing data about the user. Returns null if the
     * user cannot be found.
     * @throws SQLException Throws SQLException on SQL errors (should not happen)
     */
    public User getUserByID(int id) throws SQLException {
        try (var query = getOneOffConnection().prepareStatement(
                String.format("SELECT %s, %s, %s FROM %s WHERE %s=?", UserFields.ID, UserFields.Username,
                        UserFields.AvatarURI, UserFields.DBName, UserFields.ID))) {

            query.setInt(1, id);
            var res = query.executeQuery();
            if (res.next()) {
                return queryToUser(res);
            }
        }
        return null;
    }

    /**
     * Finds the user with the specified token.
     *
     * @param token The token of the user to look for.
     * @return A user structure containing data about the user. Returns null if the
     * user cannot be found.
     * @throws SQLException Throws SQLException on SQL errors (should not happen)
     */
    public User getUserByToken(String token) throws SQLException {
        try (var query = getOneOffConnection().prepareStatement(
                String.format("SELECT %s, %s, %s FROM %s WHERE %s = ?", UserFields.ID, UserFields.Username,
                        UserFields.AvatarURI, UserFields.DBName, UserFields.Token))) {

            query.setString(1, token);
            var res = query.executeQuery();
            if (res.next()) {
                return queryToUser(res);
            }
            return null;
        }
    }

    /**
     * Deletes the user with the specified ID.
     *
     * @param id The ID of the user to be deleted.
     * @throws SQLException Throws SQLException on SQL errors (should not happen)
     */

    // TODO: Rather than deleting the user we should mark them as deleted.
    public void deleteUser(int id) throws SQLException {
        try (var statement = getOneOffConnection()
                .prepareStatement(String.format("DELETE FROM %s WHERE %s = ?", UserFields.DBName, UserFields.ID))) {
            statement.setInt(1, id);
            statement.execute();
        }
    }

    /**
     * Gives the user specified by id the supplied token, so it can also be
     * identified based on that.
     *
     * @param id    The id of the user to give a token.
     * @param token The token to give to the user.
     * @throws SQLException            Throws SQLException on SQL errors (should not
     *                                 happen)
     * @throws NotUniqueTokenException Throws NotUniqueValueException if the token
     *                                 is already in use somewhere else. @see
     *                                 NotUniqueValueException.
     */
    public void setUserToken(int id, String token) throws SQLException {
        if (valueAlreadyExists(id, UserFields.DBName, UserFields.ID, UserFields.Token, token))
            throw new NotUniqueTokenException();

        try (var statement = getOneOffConnection().prepareStatement(String.format("UPDATE %s SET %s = ? WHERE %s = ?",
                UserFields.DBName, UserFields.Token, UserFields.ID))) {
            statement.setString(1, token);
            statement.setInt(2, id);
            statement.execute();
        }
    }

    /**
     * Gets a range (up to 100 users) of users, in sorted in ascending order of their
     * user_ids
     *
     * @param pageIdx The page to get
     * @return A list containing all the users within the page.
     * @throws SQLException Throws SQLException on SQL errors (should not happen)
     */
    public List<User> getUserRange(int pageIdx) throws SQLException {
        return getUserRange(pageIdx, 100);
    }

    // Note: This should not be used outside of tests, just keeping it here because
    // I don't want to create 100+ users for test cases.
    List<User> getUserRange(int pageIdx, int pageSize) throws SQLException {
        try (var query = getOneOffConnection().prepareStatement(String.format(
                "SELECT %s, %s, %s FROM %s ORDER BY %s LIMIT ? OFFSET ?",
                UserFields.ID, UserFields.Username,
                UserFields.AvatarURI, UserFields.DBName, UserFields.ID))) {
            query.setInt(1, pageSize);
            query.setInt(2, pageIdx * pageSize);

            var res = query.executeQuery();

            var retVal = new ArrayList<User>();
            while (res.next()) {
                retVal.add(queryToUser(res));
            }
            return retVal;
        }
    }

    /**
     * Gets extended information about a user which has the email email.
     * Extended user means, in addition to the normal fields you also get: Email, password, salt, and token.
     * NOTE: This information should be used for internal purposes only.!
     *
     * @param email The email of the user to get extended information on.
     * @return A user
     * @throws SQLException Throws SQLException on SQL errors (should not happen)
     */
    public User getExtendedInformationByEmail(String email) throws SQLException {
        try (var query = getOneOffConnection().prepareStatement(String.format("SELECT * FROM %s WHERE %s = ?",
                UserFields.DBName, UserFields.Email))) {

            query.setString(1, email);
            var res = query.executeQuery();

            if (res.next()) {
                return queryToExtendedUser(res);
            }
        }
        return null;
    }

    private User queryToUser(ResultSet set) throws SQLException {
        return new User(set.getInt(UserFields.ID),
                set.getString(UserFields.Username),
                set.getString(UserFields.AvatarURI));
    }

    private User queryToExtendedUser(ResultSet set) throws SQLException {
        return new User(set.getInt(UserFields.ID),
                set.getString(UserFields.Username),
                set.getString(UserFields.AvatarURI),
                set.getString(UserFields.Email),
                set.getString(UserFields.Salt),
                set.getString(UserFields.Token),
                set.getString(UserFields.Password));
    }

    ///////////////////////////////////////////////////////
    /// FRIENDS RELATED
    ///////////////////////////////////////////////////////
    public void createRelationship(int userID, int friendID, FriendStatus status) throws SQLException {
        try (var statement = getOneOffConnection().prepareStatement(String.format("INSERT INTO %s(%s, %s, %s) VALUES (?, ?, ?)",
                FriendFields.DBName, FriendFields.UserID, FriendFields.FriendID, FriendFields.Status))) {
            statement.setInt(1, userID);
            statement.setInt(2, friendID);
            statement.setInt(3, status.toInt());

            statement.execute();
        }
    }

    public void setRelationshipStatus(int userID, int friendID, FriendStatus status) throws SQLException {
        try (var statement = getOneOffConnection().prepareStatement(String.format("UPDATE %s SET %s = ? WHERE %s = ? AND %s = ?",
                FriendFields.DBName,
                FriendFields.Status,
                FriendFields.UserID,
                FriendFields.FriendID))) {
            statement.setInt(1, status.toInt());
            statement.setInt(2, userID);
            statement.setInt(3, friendID);

            statement.execute();
        }
    }

    /**
     * Gets the friend pair indicating the relationship between the user the id userID, and the friend with id friendID.
     *
     * @param userID
     * @param friendID
     * @return null if the friends could not be found, an array of two elements otherwise,
     * the first element contains the relationship from the user with userID's viewpoint,
     * the second element contains the relationship from the user with friendID's viewpoint.
     */
    public Relationship[] getRelationship(int userID, int friendID) throws SQLException {
        Relationship[] retVal = new Relationship[2];
        var usersToCheck = new int[]{userID, friendID};
        for (int i = 0; i < usersToCheck.length; i++) {
            try (var query = getOneOffConnection().prepareStatement(String.format("SELECT %s, %s, %s, %s FROM %s WHERE (%s = ? AND %s = ?)",
                    FriendFields.ID,
                    FriendFields.UserID, FriendFields.FriendID, FriendFields.Status, FriendFields.DBName,
                    FriendFields.UserID, FriendFields.FriendID))) {
                query.setInt(1, usersToCheck[i]);
                query.setInt(2, usersToCheck[(i + 1) % usersToCheck.length]);

                var res = query.executeQuery();

                if (res.next()) {
                    retVal[i] = new Relationship(res.getInt(FriendFields.ID), res.getInt(FriendFields.UserID),
                            res.getInt(FriendFields.FriendID),
                            FriendStatus.fromInt(res.getInt(FriendFields.Status)));
                }
            }
        }

        return (retVal[0] == null) ? null : retVal;
    }

    // Note: This should not be used outside of tests, just keeping it here because
    // I don't want to create 100+ users for test cases.
    public List<Friend> getFriendsRange(int userID, int pageIndex) throws SQLException {
        return getFriendsRange(userID, pageIndex, 100);
    }

    public List<Friend> getFriendsRange(int userID, int pageIndex, int pageSize) throws SQLException {
        // SELECT friend.friend_id, friend.status, user.username FROM friend INNER JOIN user ON friend.friend_id = user.user_id WHERE friend.user_id = 1 AND friend.status != ? ORDER BY friend.user_id LIMIT 100 OFFSET 0
        var queryCommand = String.format("SELECT %s.%s, %s.%s, %s.%s FROM %s INNER JOIN %s ON %s.%s = %s.%s WHERE %s.%s = ? AND %s.%s != ? ORDER BY %s.%s LIMIT ? OFFSET ?",
                FriendFields.DBName, FriendFields.FriendID,
                FriendFields.DBName, FriendFields.Status,
                UserFields.DBName, UserFields.Username,
                FriendFields.DBName, UserFields.DBName,
                FriendFields.DBName, FriendFields.FriendID,
                UserFields.DBName, UserFields.ID,
                FriendFields.DBName, FriendFields.UserID,
                FriendFields.DBName, FriendFields.Status,
                FriendFields.DBName, FriendFields.UserID);

        try (var query = getOneOffConnection().prepareStatement(queryCommand)) {

            query.setInt(1, userID);
            query.setInt(2, FriendStatus.UNFRIENDED.toInt());
            query.setInt(3, pageSize);
            query.setInt(4, pageSize * pageIndex);

            var res = query.executeQuery();

            var retVal = new ArrayList<Friend>();
            while (res.next()) {
                retVal.add(new Friend(res.getInt(FriendFields.FriendID), res.getString(UserFields.Username), FriendStatus.fromInt(res.getInt(FriendFields.Status))));
            }

            return retVal;
        }
    }

    ///////////////////////////////////////////////////////
    /// CHAT RELATED
    ///////////////////////////////////////////////////////
    public int createChat(String name) throws SQLException {
        try (var connection = getMultiStatementConnection()) {
            try (var stmt = connection.prepareStatement(String.format("INSERT INTO %s(%s) VALUES(?)", ChatFields.DBName, ChatFields.Name), Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, name);
                stmt.execute();
                var res = stmt.getGeneratedKeys();
                res.next();
                return res.getInt(1);
            }
        }
    }

    public void logChatMessage(int userID, int chatID, String message) throws SQLException {
        try (var statement = getOneOffConnection().prepareStatement(String.format("INSERT INTO %s(%s, %s, %s) VALUES(?, ?, ?)",
                ChatMessageFields.DBName, ChatMessageFields.ChatID, ChatMessageFields.UserID, ChatMessageFields.Message))) {
            statement.setInt(1, chatID);
            statement.setInt(2, userID);
            statement.setString(3, message);
            statement.execute();
        }
    }


    ///////////////////////////////////////////////////////
    /// TABLE CREATION RELATED
    ///////////////////////////////////////////////////////
    private void createUserTable() throws SQLException {
        String command = "CREATE TABLE IF NOT EXISTS " + UserFields.DBName + "(" + UserFields.ID
                + " INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, " + UserFields.Username + " text NOT NULL, "
                + UserFields.Email + " text NOT NULL UNIQUE, " + UserFields.Password + " text NOT NULL, " // Hashed password!
                + UserFields.Salt + " text NOT NULL, "
                + UserFields.AvatarURI + " text DEFAULT(NULL), " + UserFields.Token + " text DEFAULT(NULL)" + ");";

        var statement = getOneOffConnection().createStatement();
        statement.execute(command);
    }

    private void createFriendsTable() throws SQLException {
        String command = "CREATE TABLE IF NOT EXISTS " + FriendFields.DBName + "("
                + FriendFields.ID + " INTEGER NOT NULL PRIMARY KEY, "
                + FriendFields.UserID + " INTEGER NOT NULL, "
                + FriendFields.FriendID + " INTEGER NOT NULL, "
                + FriendFields.Status + " INTEGER NOT NULL, "
                + "FOREIGN KEY (" + FriendFields.UserID + ") REFERENCES " + UserFields.DBName + "(" + UserFields.ID + ") ON DELETE CASCADE,"
                + "FOREIGN KEY (" + FriendFields.FriendID + ") REFERENCES " + UserFields.DBName + "(" + UserFields.ID + ") ON DELETE CASCADE"
                + ");";

        var statement = getOneOffConnection().createStatement();
        statement.execute(command);
    }

    private void createChatMessagesTable() throws SQLException {
        String command = "CREATE TABLE IF NOT EXISTS " + ChatMessageFields.DBName + "("
                + ChatMessageFields.ID + " INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, "
                + ChatMessageFields.ChatID + " INTEGER NOT NULL, "
                + ChatMessageFields.UserID + " INTEGER NOT NULL, "
                + ChatMessageFields.Message + " TEXT NOT NULL, "
                + ChatMessageFields.Timestamp + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
                + "FOREIGN KEY (" + ChatMessageFields.ChatID + ") REFERENCES " + ChatFields.DBName + "(" + ChatFields.ID + ") ON DELETE CASCADE,"
                + "FOREIGN KEY (" + ChatMessageFields.UserID + ") REFERENCES " + UserFields.DBName + "(" + UserFields.ID + ") ON DELETE CASCADE"
                + ");";

        var statement = getOneOffConnection().createStatement();
        statement.execute(command);
    }

    private void createChatTable() throws SQLException {
        // Specifically checking for this first, as we want the global chat to always have the value 0.
        var query = getOneOffConnection().createStatement();
        var res = query.executeQuery(String.format("SELECT name FROM sqlite_master WHERE type = 'table' AND name = '%s'",
                ChatFields.DBName));

        // We already have this table.
        if (res.next()) {
            return;
        }

        String command = "CREATE TABLE IF NOT EXISTS " + ChatFields.DBName + "("
                + ChatFields.ID + " INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, "
                + ChatFields.Name + " TEXT NOT NULL);";

        var statement = getOneOffConnection().createStatement();
        statement.execute(command);

        createChat(GlobalChat.NAME);
    }

    ///////////////////////////////////////////////////////
    /// Utility
    ///////////////////////////////////////////////////////
    private boolean valueAlreadyExists(int id, String table, String idField, String field, String value)
            throws SQLException {
        try (var query = getOneOffConnection()
                .prepareStatement(String.format("SELECT %s FROM %s WHERE %s=?", idField, table, field))) {
            query.setString(1, value);
            var res = query.executeQuery();
            return res.next() && res.getInt(idField) != id;
        }
    }

    ///////////////////////////////////////////////////////
    /// RETURN CLASSES
    ///////////////////////////////////////////////////////

    /**
     * Class representing a relationship, from the viewpoint of the user with userID.
     * POD class by design, as there are no invariants to protect in this class.
     */
    public class Relationship {
        public int rowID;
        public int userID;
        public int friendID;
        public FriendStatus status;

        Relationship(int rowID, int userID, int friendID, FriendStatus status) {
            this.rowID = rowID;
            this.userID = userID;
            this.friendID = friendID;
            this.status = status;
        }
    }

    /**
     * Class representing a user but from a friend perspective, so it contains the userID, username, and the FriendStatus of this friend.
     * POD class by design, as there are no invariants to protect in this class.
     */
    public class Friend {
        public int userID;
        public String username;
        public FriendStatus status;

        Friend(int userID, String username, FriendStatus status) {
            this.userID = userID;
            this.username = username;
            this.status = status;
        }
    }

    /**
     * Class representing a user.
     * POD class by design, as there are no invariants to protect in this class.
     */
    public static class User {
        public User(int id, String username, String avatarURI) {
            this.id = id;
            this.username = username;
            this.avatarURI = (avatarURI != null) ? avatarURI : "";
        }

        public User(int id, String username, String avatarURI, String email, String salt, String token, String password) {
            this.id = id;
            this.username = username;
            this.email = email;
            this.avatarURI = (avatarURI != null) ? avatarURI : "";
            this.salt = salt;
            this.token = token;
            this.password = password;
        }

        public int id = -1;
        public String username = "";
        public String avatarURI = "";

        /**
         * The email of the user, this will be null except when getExtendedInformation has been called.
         * Should not be exposed to the outside world!
         */
        public String email = "";


        /**
         * The salt used to hash the password, this will be null except when getExtendedInformation has been called.
         * Should not be exposed to the outside world!
         */
        public String salt = "";

        /**
         * The authentication token of the user, this will be null except when getExtendedInformation has been called.
         * Should not be exposed to the outside world!
         */
        public String token = "";

        /**
         * The hashed password of the user, this will be null except when getExtendedInformation has been called.
         * Should not be exposed to the outside world!
         */
        public String password = "";
    }
}
