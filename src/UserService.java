import java.sql.*;
import java.util.Scanner;

class UserService {
    private static final String SQL_REGISTER_USER = "INSERT INTO users (full_name, email, password, role) VALUES (?, ?, ?, ?)";
    private static final String SQL_LOGIN_USER = "SELECT user_id, full_name, email, password, role FROM users WHERE email = ? AND password = ?";
    private static final String SQL_CHECK_EMAIL = "SELECT user_id FROM users WHERE email = ?";
    private static final String SQL_UPDATE_USER_DETAILS = "UPDATE users SET full_name = ?, email = ?, password = ? WHERE user_id = ?";
    private static final String SQL_DELETE_USER = "DELETE FROM users WHERE user_id = ?";

    private Scanner scanner;

    public UserService(Scanner scanner) {
        this.scanner = scanner;
    }

    public boolean checkEmailExists(String email) {
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SQL_CHECK_EMAIL)) {

            pstmt.setString(1, email);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next(); // True if a user is found
            }
        } catch (SQLException e) {
            System.err.println("Database error during email check: " + e.getMessage());
            return false;
        }
    }

    public Person registerUser(String role) {
        System.out.print("Enter Full Name: ");
        String name = scanner.nextLine();
        String email;
        do {
            System.out.print("Enter Email (Must be unique): ");
            email = scanner.nextLine();
            if (checkEmailExists(email)) {
                System.out.println("Email already registered. Please use a different email.");
            } else {
                break;
            }
        } while (true);
        System.out.print("Enter Password: ");
        String password = scanner.nextLine();

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SQL_REGISTER_USER, Statement.RETURN_GENERATED_KEYS)) {

            conn.setAutoCommit(false); // Start transaction

            pstmt.setString(1, name);
            pstmt.setString(2, email);
            pstmt.setString(3, password);
            pstmt.setString(4, role);

            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        int userId = rs.getInt(1);
                        System.out.println(role + " registered successfully. User ID: " + userId);

                        // Proceed to link to Employee/Customer table
                        Person newPerson = null;
                        if (role.equals("CUSTOMER")) {
                            CustomerService customerService = new CustomerService(this.scanner);
                            newPerson = customerService.createCustomer(userId, name, email, password);
                        } else if (role.equals("EMPLOYEE")) {
                            newPerson = new EmployeeService().createEmployee(userId, name, email, password);
                        }

                        conn.commit(); // Commit transaction only if both inserts succeed
                        return newPerson;
                    }
                }
            }
            conn.rollback(); // Rollback if something failed

        } catch (SQLException e) {
            System.err.println("Database error during registration: " + e.getMessage());
        }
        return null;
    }

    public Person loginUser() {
        System.out.print("Enter Email: ");
        String email = scanner.nextLine();
        System.out.print("Enter Password: ");
        String password = scanner.nextLine();

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SQL_LOGIN_USER)) {

            pstmt.setString(1, email);
            pstmt.setString(2, password);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int userId = rs.getInt("user_id");
                    String fullName = rs.getString("full_name");
                    String role = rs.getString("role");

                    if (role.equals("CUSTOMER")) {
                        return new CustomerService(this.scanner).getCustomerByUserId(userId);
                    } else if (role.equals("EMPLOYEE")) {
                        return new EmployeeService().getEmployeeByUserId(userId);
                    }
                } else {
                    System.out.println("Invalid email or password.");
                }
            }
        } catch (SQLException e) {
            System.err.println("Database error during login: " + e.getMessage());
        }
        return null;
    }

    /**
     * **NEW: Update User Details**
     * Updates the common user details (name, email, password) in the users table.
     */
    public boolean updateUser(Person person) {
        // NOTE: Email uniqueness check should be handled in the menu before calling this.
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SQL_UPDATE_USER_DETAILS)) {

            pstmt.setString(1, person.getFullName());
            pstmt.setString(2, person.getEmail());
            pstmt.setString(3, person.getPassword());
            pstmt.setInt(4, person.getUserId());

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Database error updating user details: " + e.getMessage());
            return false;
        }
    }

    /**
     * **NEW: Delete User**
     * Deletes the user record from the users table. 
     * NOTE: Database foreign key constraints should handle cascading deletes for customers/employees/accounts/transactions.
     */
    public boolean deleteUser(int userId) {
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SQL_DELETE_USER)) {

            pstmt.setInt(1, userId);
            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Database error deleting user: " + e.getMessage());
            return false;
        }
    }
}