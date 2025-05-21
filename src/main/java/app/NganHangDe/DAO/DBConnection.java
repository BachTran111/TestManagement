package app.NganHangDe.DAO;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.io.InputStream;
import java.io.FileInputStream;

public class DBConnection {
    private static Connection instance = null;

    private DBConnection() {}

    public static Connection getConnection() throws SQLException {
        if (instance == null || instance.isClosed()) {
            Properties props = new Properties();
            try (InputStream input = new FileInputStream("src/main/resources/config.properties")) {
                props.load(input);

                String url = props.getProperty("db.url");
                String user = props.getProperty("db.user");
                String password = props.getProperty("db.password");

                instance = DriverManager.getConnection(url, user, password);
            } catch (Exception e) {
                throw new SQLException("Loi db", e);
            }
        }
        return instance;
    }

    public static void closeConnection() {
        try {
            if (instance != null && !instance.isClosed()) {
                instance.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
