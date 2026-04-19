import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;

/**
 * Lightweight AuthService used by the Swing UI.
 * Uses DatabaseConfig to insert/login users and delegates creation of customer/employee
 * details to CustomerService / EmployeeService so existing logic/database code is reused.
 *
 * NOTE: This class avoids Scanner-based interactive prompts and accepts parameters from the UI.
 */
class AuthService {
    private static final String SQL_REGISTER_USER = "INSERT INTO users (full_name, email, password, role) VALUES (?, ?, ?, ?)";
    private static final String SQL_LOGIN_USER = "SELECT user_id, full_name, email, password, role FROM users WHERE email = ? AND password = ?";

    // Non-interactive login used by UI
    public Person login(String email, String password) throws SQLException {
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SQL_LOGIN_USER)) {
            pstmt.setString(1, email);
            pstmt.setString(2, password);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (!rs.next()) return null;
                int userId = rs.getInt("user_id");
                String role = rs.getString("role");
                if ("CUSTOMER".equalsIgnoreCase(role)) {
                    // Return the Customer object built by CustomerService
                    return new CustomerService(new Scanner(System.in)).getCustomerByUserId(userId);
                } else if ("EMPLOYEE".equalsIgnoreCase(role)) {
                    return new EmployeeService().getEmployeeByUserId(userId);
                } else {
                    return null;
                }
            }
        }
    }

    /**
     * Registers a new customer user and creates the associated customer row.
     * All DB work is transactional to avoid partial inserts.
     */
    public Customer registerCustomer(String fullName, String email, String password, String address, String phone) throws SQLException {
        try (Connection conn = DatabaseConfig.getConnection()) {
            conn.setAutoCommit(false);
            int userId;

            try (PreparedStatement up = conn.prepareStatement(SQL_REGISTER_USER, Statement.RETURN_GENERATED_KEYS)) {
                up.setString(1, fullName);
                up.setString(2, email);
                up.setString(3, password);
                up.setString(4, "CUSTOMER");

                if (up.executeUpdate() == 0) {
                    conn.rollback();
                    return null;
                }

                try (ResultSet keys = up.getGeneratedKeys()) {
                    if (!keys.next()) {
                        conn.rollback();
                        return null;
                    }
                    userId = keys.getInt(1);
                }
            }

            String SQL_CREATE_CUSTOMER = "INSERT INTO customers (user_id, address, phone_number) VALUES (?, ?, ?)";
            try (PreparedStatement cp = conn.prepareStatement(SQL_CREATE_CUSTOMER, Statement.RETURN_GENERATED_KEYS)) {
                cp.setInt(1, userId);
                cp.setString(2, address);
                cp.setString(3, phone);

                if (cp.executeUpdate() == 0) {
                    conn.rollback();
                    return null;
                }
            }

            conn.commit();
            return new CustomerService(new Scanner(System.in)).getCustomerByUserId(userId);
        }
    }

    /**
     * Registers a new employee user and creates the associated employee row.
     */
    public Employee registerEmployee(String fullName, String email, String password, String jobTitle, double salary) throws SQLException {
        try (Connection conn = DatabaseConfig.getConnection()) {
            conn.setAutoCommit(false);
            int userId;

            try (PreparedStatement up = conn.prepareStatement(SQL_REGISTER_USER, Statement.RETURN_GENERATED_KEYS)) {
                up.setString(1, fullName);
                up.setString(2, email);
                up.setString(3, password);
                up.setString(4, "EMPLOYEE");

                if (up.executeUpdate() == 0) {
                    conn.rollback();
                    return null;
                }

                try (ResultSet keys = up.getGeneratedKeys()) {
                    if (!keys.next()) {
                        conn.rollback();
                        return null;
                    }
                    userId = keys.getInt(1);
                }
            }

            String SQL_CREATE_EMPLOYEE = "INSERT INTO employees (user_id, job_title, salary) VALUES (?, ?, ?)";
            try (PreparedStatement ep = conn.prepareStatement(SQL_CREATE_EMPLOYEE, Statement.RETURN_GENERATED_KEYS)) {
                ep.setInt(1, userId);
                ep.setString(2, jobTitle);
                ep.setDouble(3, salary);

                if (ep.executeUpdate() == 0) {
                    conn.rollback();
                    return null;
                }
            }

            conn.commit();
            return new EmployeeService().getEmployeeByUserId(userId);
        }
    }
}