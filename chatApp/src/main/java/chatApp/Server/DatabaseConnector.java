package chatApp.Server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnector {
    private Connection connection;
    private static final String DB_URL = "jdbc:sqlite:chatapp.db";

    public DatabaseConnector() throws SQLException {
        connection = DriverManager.getConnection(DB_URL);
    }

    public Connection getConnection() {
        return connection;
    }

    // ... other related methods
}