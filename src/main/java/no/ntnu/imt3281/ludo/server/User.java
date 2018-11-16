package no.ntnu.imt3281.ludo.server;

/*
 * POD for containing user data, all fields are public by intention.
 */
public class User {
    @Deprecated
    private User() {

    }

    public User(int id, String username, String avatarURI) {
        this.id = id;
        this.username = username;
        this.avatarURI = avatarURI;
    }

    public User(int id, String username, String avatarURI, String email, String salt, String token, String password) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.avatarURI = avatarURI;
        this.salt = salt;
        this.token = token;
        this.password = password;
    }

    public int id;
    public String username;
    public String avatarURI;

    /**
     * The email of the user, this will be null except when getExtendedInformation has been called.
     * Should not be exposed to the outside world!
     */
    public String email;


    /**
     * The salt used to hash the password, this will be null except when getExtendedInformation has been called.
     * Should not be exposed to the outside world!
     */
    public String salt;

    /**
     * The authentication token of the user, this will be null except when getExtendedInformation has been called.
     * Should not be exposed to the outside world!
     */
    public String token;

    /**
     * The hashed password of the user, this will be null except when getExtendedInformation has been called.
     * Should not be exposed to the outside world!
     */
    public String password;
}
