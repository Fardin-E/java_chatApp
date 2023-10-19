package chatApp.Server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnector {
    private static final String DATABASE_URL = "jdbc:sqlite:chatapp.db";
    private static final String TEST_DATABASE_URL = "jdbc:sqlite:chatapp-test.db";
    
    private boolean isTestMode;

    public DatabaseConnector() {
        this(false);
    }

    public DatabaseConnector(boolean isTestMode) {
        this.isTestMode = isTestMode;
    }

    public Connection getConnection() throws SQLException {
        if (isTestMode) {
            return connectToTestDatabase();
        } else {
            return connectToMainDatabase();
        }
    }
    
    private Connection connectToMainDatabase() throws SQLException {
        try {
            Class.forName("org.sqlite.JDBC");
            return DriverManager.getConnection(DATABASE_URL);
        } catch (ClassNotFoundException e) {
            throw new SQLException("Failed to load SQLite JDBC driver.", e);
        }
    }
    
    private Connection connectToTestDatabase() throws SQLException {
        try {
            Class.forName("org.sqlite.JDBC");
            return DriverManager.getConnection(TEST_DATABASE_URL);
        } catch (ClassNotFoundException e) {
            throw new SQLException("Failed to load SQLite JDBC driver.", e);
        }
    }
}