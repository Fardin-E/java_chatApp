package chatApp;

import chatApp.Server.UserManager;
import junit.framework.TestCase;
import org.junit.jupiter.api.Test;

public class UserManagerTest extends TestCase {

    private UserManager userManager;

    @Override
    protected void setUp() throws Exception {
        // Using test database
        userManager = new UserManager(true);
        
        // Setup test database schema
        userManager.createUserTable();
        
        // Add mock data
        userManager.createUser("testUser", "testPassword");
    }

    @Override
    protected void tearDown() throws Exception {
        // Clean up after test
        userManager.dropUserTable();
    }

    public void testCorrectAuthentication() {
        UserManager.AuthResult result = userManager.authenticateUser("testUser", "testPassword");
        assertEquals(UserManager.AuthResult.SUCCESS, result);
    }

    public void testWrongPasswordAuthentication() {
        UserManager.AuthResult result = userManager.authenticateUser("testUser", "wrongPassword");
        assertEquals(UserManager.AuthResult.WRONG_PASSWORD, result);
    }

    public void testNonExistentUserAuthentication() {
        UserManager.AuthResult result = userManager.authenticateUser("nonExistentUser", "anyPassword");
        assertEquals(UserManager.AuthResult.WRONG_USERNAME, result);
    }

    // ... Add more tests as needed

}