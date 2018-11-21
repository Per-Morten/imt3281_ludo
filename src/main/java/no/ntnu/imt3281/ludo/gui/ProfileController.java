package no.ntnu.imt3281.ludo.gui;

public class ProfileController {

    /**
     * Creates a new user with the given name, email and password. Error if email
     * already exists
     *
     * @param username
     * @param email
     * @param passwordHash
     */
    public void createUser(String username, String email, String passwordHash) {

    }

    /**
     * Login the user using email and password. If already logged in, refresh
     * authentication token. Already logged in is not an error. Error if wrong email
     * or password
     *
     * @param email
     * @param passwordHash
     */
    public void login(String email, String passwordHash) {

    }

    /**
     * Assumes the server checked your authentication Logout user. Already logged
     * out is not an error.
     */
    public void logout() {

    }

    /**
     * Assumes the server checked your authentication. Returns data about the user
     * indicated by userID
     *
     * @param userID the ID of the user to be fetched
     */
    public void getUser(int userID) {

    }

    /**
     * Assumes the server checked your authentication. Update user information.
     * Error if invalid email
     *
     * @param id
     * @param username
     * @param email
     * @param passwordHash
     */
    public void updateUser(int id, String username, String email, String passwordHash) {

    }

    /**
     * Assumes the server checked your authentication. Deletes user. Error if user
     * not found
     *
     * @param id
     * @param user_id
     */
    public void deleteUser(int id, int user_id) {

    }
}
