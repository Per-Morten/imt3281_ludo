package no.ntnu.imt3281.ludo.server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

// TODO: Do validation myself, such a pain to deal with the SQLExceptions and give proper errors when it is just 1 Exception covering everything.
//
//
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
    }

    private static final int UnassignedID = -1;

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
        String url = "jdbc:sqlite:" + dbFilename;

        String user = "CREATE TABLE IF NOT EXISTS " + UserFields.DBName + "(" + UserFields.ID
                + " INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, " + UserFields.Username + " text NOT NULL, "
                + UserFields.Email + " text NOT NULL UNIQUE, " + UserFields.Password + " text NOT NULL, " // Hashed
                                                                                                          // password!
                + UserFields.AvatarURI + " text DEFAULT(NULL), " + UserFields.Token + " text DEFAULT(NULL)" + ");";

        mConnection = DriverManager.getConnection(url);
        var statement = mConnection.createStatement();
        statement.execute(user);
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
    /// Utility
    ///////////////////////////////////////////////////////
    private boolean valueAlreadyExists(int id, String table, String idField, String field, String value)
            throws SQLException {
        try (var query = mConnection
                .prepareStatement(String.format("SELECT %s FROM %s WHERE %s=?", idField, table, field))) {
            query.setString(1, value);
            var res = query.executeQuery();
            return res.next() && res.getInt(idField) != id;
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
     * @throws SQLException            Throws SQLException Upon SQL Errors (Should
     *                                 not happen)
     * @throws NotUniqueValueException Throws NotUniqueValueException in the case
     *                                 where a field that is required to be Unique
     *                                 isn't unique. @see NotUniqueValueException
     */
    public int createUser(String username, String email, String password) throws SQLException {
        if (valueAlreadyExists(UnassignedID, UserFields.DBName, UserFields.ID, UserFields.Email, email))
            throw new NotUniqueValueException(UserFields.Email);

        if (valueAlreadyExists(UnassignedID, UserFields.DBName, UserFields.ID, UserFields.Username, username))
            throw new NotUniqueValueException(UserFields.Username);

        try (var statement = mConnection.prepareStatement(String.format("INSERT INTO %s(%s, %s, %s) VALUES (?, ?, ?)",
                UserFields.DBName, UserFields.Username, UserFields.Email, UserFields.Password))) {
            statement.setString(1, username);
            statement.setString(2, email);
            statement.setString(3, password);

            statement.execute();
        }

        // Need to get the id so it can be sent back
        try (var query = mConnection.prepareStatement(
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
     * @throws SQLException            Throws SQLException on SQL errors (should not
     *                                 happen)
     * @throws NotUniqueValueException Throws NotUniqueValueException in the case
     *                                 where a field that is required to be Unique
     *                                 isn't unique. @see NotUniqueValueException
     */
    public void updateUser(int id, String username, String email, String password, String avatarURI)
            throws SQLException {
        if (valueAlreadyExists(id, UserFields.DBName, UserFields.ID, UserFields.Email, email))
            throw new NotUniqueValueException(UserFields.Email);

        if (valueAlreadyExists(UnassignedID, UserFields.DBName, UserFields.ID, UserFields.Username, username))
            throw new NotUniqueValueException(UserFields.Username);

        try (var statement = mConnection.prepareStatement(String.format(
                "UPDATE %s SET %s = ?, %s = ?, %s = ?, %s = ? WHERE %s=?", UserFields.DBName, UserFields.Username,
                UserFields.Email, UserFields.Password, UserFields.AvatarURI, UserFields.ID))) {

            statement.setString(1, username);
            statement.setString(2, email);
            statement.setString(3, password);
            statement.setString(4, avatarURI);
            statement.setInt(5, id);
            statement.execute();
        }
    }

    /**
     * Finds the user with the specified user_id.
     *
     * @param id The user_id of the user to look for.
     * @return A user structure containing data about the user. Returns null if the
     *         user cannot be found.
     * @throws SQLException Throws SQLException on SQL errors (should not happen)
     */
    public User getUserByID(int id) throws SQLException {
        try (var query = mConnection.prepareStatement(
                String.format("SELECT %s, %s, %s, %s FROM %s WHERE %s=?", UserFields.ID, UserFields.Username,
                        UserFields.Email, UserFields.AvatarURI, UserFields.DBName, UserFields.ID))) {

            query.setInt(1, id);
            var res = query.executeQuery();
            if (res.next()) {
                User user = new User();
                user.id = res.getInt(UserFields.ID);
                user.username = res.getString(UserFields.Username);
                user.avatarURI = res.getString(UserFields.AvatarURI);
                user.email = res.getString(UserFields.Email);
                return user;
            }
        }
        return null;
    }

    /**
     * Finds the user with the specified token.
     *
     * @param token The token of the user to look for.
     * @return A user structure containing data about the user. Returns null if the
     *         user cannot be found.
     * @throws SQLException Throws SQLException on SQL errors (should not happen)
     */
    public User getUserByToken(String token) throws SQLException {
        try (var query = mConnection.prepareStatement(
                String.format("SELECT %s, %s, %s, %s FROM %s WHERE %s = ?", UserFields.ID, UserFields.Username,
                        UserFields.Email, UserFields.AvatarURI, UserFields.DBName, UserFields.Token))) {

            query.setString(1, token);
            var res = query.executeQuery();
            if (res.next()) {
                User user = new User();
                user.id = res.getInt(UserFields.ID);
                user.username = res.getString(UserFields.Username);
                user.avatarURI = res.getString(UserFields.AvatarURI);
                user.email = res.getString(UserFields.Email);
                return user;
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
    public void deleteUser(int id) throws SQLException {
        try (var statement = mConnection
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
     * @throws NotUniqueValueException Throws NotUniqueValueException if the token
     *                                 is already in use somewhere else. @see
     *                                 NotUniqueValueException.
     */
    public void setUserToken(int id, String token) throws SQLException {
        if (valueAlreadyExists(id, UserFields.DBName, UserFields.ID, UserFields.Token, token))
            throw new NotUniqueValueException(UserFields.Token);

        try (var statement = mConnection.prepareStatement(String.format("UPDATE %s SET %s = ? WHERE %s = ?",
                UserFields.DBName, UserFields.Token, UserFields.ID))) {
            statement.setString(1, token);
            statement.setInt(2, id);
            statement.execute();
        }
    }

    /**
     * Gets a page (up to 100 users) of users, in sorted in ascending order of their
     * user_ids
     *
     * @param pageIdx The page to get
     * @return A list containing all the users within the page.
     * @throws SQLException Throws SQLException on SQL errors (should not happen)
     */
    public List<User> getUserPage(int pageIdx) throws SQLException {
        return getUserPage(pageIdx, 100);
    }

    // Note: This should not be used outside of tests, just keeping it here because
    // I don't want to create 100+ users for test cases.
    List<User> getUserPage(int pageIdx, int pageSize) throws SQLException {
        try (var query = mConnection.prepareStatement(String.format(
                "SELECT %s, %s, %s, %s FROM %s WHERE %s BETWEEN ? AND ?", UserFields.ID, UserFields.Username,
                UserFields.Email, UserFields.AvatarURI, UserFields.DBName, UserFields.ID))) {
            query.setInt(1, (pageIdx * pageSize) + 1);
            query.setInt(2, (pageIdx + 1) * pageSize);

            var res = query.executeQuery();

            var retVal = new ArrayList<User>();
            while (res.next()) {
                User user = new User();
                user.id = res.getInt(UserFields.ID);
                user.username = res.getString(UserFields.Username);
                user.avatarURI = res.getString(UserFields.AvatarURI);
                user.email = res.getString(UserFields.Email);
                retVal.add(user);
            }
            return retVal;
        }
    }
}
