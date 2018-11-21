package no.ntnu.imt3281.ludo.server;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import no.ntnu.imt3281.ludo.api.Error;

public class DatabaseTest {
    private static Database sDB;

    @BeforeClass
    public static void SetupDatabase() throws IOException, SQLException {
        Files.deleteIfExists(Paths.get("ludo_tests.db"));

        sDB = new Database("ludo_tests.db");

        // Don't re-order these, I rely on their autoincremented id for a lot of my tests.
        sDB.createUser("User1", "User1@mail.com", "User1Password", "salt"); // id = 1
        sDB.createUser("User2", "User2@mail.com", "User2Password", "salt"); // id = 2
        sDB.createUser("User3", "User3@mail.com", "User3Password", "salt"); // id = 3
        sDB.createUser("User4", "User4@mail.com", "User4Password", "salt"); // id = 3
        sDB.createUser("User5", "User5@mail.com", "User5Password", "salt"); // id = 3
    }

    @AfterClass
    public static void shutDownDatabase() throws SQLException {
        sDB.close();
    }

    ///////////////////////////////////////////////////////
    /// User Tests
    ///////////////////////////////////////////////////////
    @Test
    public void canCreateUser() throws SQLException {
        sDB.createUser("NewUser", "NewUser@outlook.com", "something", "salt");
    }

    @Test
    public void canRetrieveUser() throws SQLException {
        var user = sDB.getUserByID(1);
        assertEquals(1, user.id);
        assertEquals("User1", user.username);
        assertEquals("", user.avatarURI);
    }

    @Test
    public void canDeleteUser() throws SQLException {
        var id = sDB.createUser("UserToDelete", "UserToDelete@mail.com", "UserToDelete", "salt");
        sDB.deleteUser(id);
        assertNull(sDB.getUserByID(id));
    }

    @Test
    public void canUpdateUser() throws SQLException {
        var id = sDB.createUser("UserToUpdate", "UserToUpdate@mail.com", "UserToUpdate", "salt");
        sDB.updateUser(id, "UpdatedUser", "UserToUpdate@mail.com", "pwd", null, "salt");
        var user = sDB.getUserByID(id);
        assertEquals("UpdatedUser", user.username);
        assertEquals("", user.avatarURI);
    }

    @Test
    public void canGetUserIDBasedWithToken() throws SQLException {
        var id = sDB.createUser("UserToGiveToken", "UserToGiveToken@mail.com", "pwd", "salt");
        sDB.setUserToken(id, "UserToGiveToken");
        var user = sDB.getUserByToken("UserToGiveToken");
        assertNotNull(user);
        assertEquals(id, user.id);
    }

    @Test
    public void canGetPage() throws SQLException {
        {
            var users = sDB.getUserRange(0, 3);
            assertEquals("User1",users.get(0).username);
            assertEquals("User2",users.get(1).username);
            assertEquals("User3",users.get(2).username);
        }

        {
            var users = sDB.getUserRange(1, 3);
            assertEquals("User4",users.get(0).username);
            assertEquals("User5",users.get(1).username);

        }
    }

    ///////////////////////////////////////////////////////
    /// User Exception Tests
    ///////////////////////////////////////////////////////
    @Test
    public void throwsOnNonUniqueEmailInCreation() throws SQLException {
        try {
            sDB.createUser("NonUnique", "User1@mail.com", "something", "salt");
        } catch (APIErrorException e) {
            assertEquals(Error.NOT_UNIQUE_EMAIL, e.getError());
        }
    }

    @Test
    public void throwsOnNonUniqueEmailInUpdate() throws SQLException {
        try {
            sDB.updateUser(2, "User2", "User1@mail.com", "pwd", null, "salt");
        } catch (APIErrorException e) {
            assertEquals(Error.NOT_UNIQUE_EMAIL, e.getError());
        }
    }

    @Test(expected = NotUniqueTokenException.class)
    public void throwsOnNonUniqueToken() throws SQLException {

            sDB.setUserToken(1, "Token");
            sDB.setUserToken(2, "Token");

    }
}
