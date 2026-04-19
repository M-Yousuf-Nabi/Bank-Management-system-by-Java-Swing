import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

class DatabaseConfig {
    private static final String URL = "jdbc:mysql://crossover.proxy.rlwy.net:44891/railway";
    private static final String USER = "root"; // Your MySQL Username
    private static final String PASSWORD = "FWAvdPbcIXPMVDXMJtzivlWEqqNUkAwz";  // Your MySQL Password

    private DatabaseConfig() {}

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL JDBC Driver not found.");
            throw new SQLException(e);
        }
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
