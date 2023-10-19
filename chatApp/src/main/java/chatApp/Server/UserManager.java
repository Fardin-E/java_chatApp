package chatApp.Server;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


public class UserManager {
    private Connection connection;

    public UserManager() throws SQLException {
        this(false);
    }

    public UserManager(boolean isTestMode) throws SQLException {
        DatabaseConnector dbConnector = new DatabaseConnector(isTestMode);
        connection = dbConnector.getConnection();
    }

    public void createUserTable() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS users (" +
                 "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                 "username TEXT NOT NULL UNIQUE," +
                 "hashedPassword TEXT NOT NULL)";
        java.sql.Statement stmt = connection.createStatement();
        stmt.execute(sql);
    }

    public void createUser(String username, String password) throws SQLException {
        String sql = "INSERT INTO users(username, hashedPassword) VALUES(?,?)";
    
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setString(2, password);  // Storing the password directly for simplicity.
            stmt.executeUpdate();
        }
    }

    public void dropUserTable() throws SQLException {
        String sql = "DROP TABLE IF EXISTS users";
        java.sql.Statement stmt = connection.createStatement();
        stmt.execute(sql);
    }
    public enum AuthResult {
        SUCCESS,
        WRONG_USERNAME,
        WRONG_PASSWORD,
        ERROR
    }

    public AuthResult authenticateUser(String username, String password) {
        String query = "SELECT hashedPassword FROM users WHERE username = ?";
    
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, username);
            
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                String storedPassword = resultSet.getString("hashedPassword");
                if (storedPassword.equals(password)) {
                    return AuthResult.SUCCESS;
                } else {
                    return AuthResult.WRONG_PASSWORD;
                }
            } else {
                return AuthResult.WRONG_USERNAME;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return AuthResult.ERROR; // To handle general database errors
        }
    }    

}
