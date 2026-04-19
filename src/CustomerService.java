import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

class CustomerService {
    private static final String SQL_CREATE_CUSTOMER = "INSERT INTO customers (user_id, address, phone_number) VALUES (?, ?, ?)";
    private static final String SQL_GET_CUSTOMER_BY_USER_ID = "SELECT c.customer_id, c.address, c.phone_number, u.full_name, u.email, u.password " +
            "FROM customers c JOIN users u ON c.user_id = u.user_id WHERE c.user_id = ?";
    private static final String SQL_GET_ALL_CUSTOMERS = "SELECT c.customer_id, c.address, c.phone_number, u.user_id, u.full_name, u.email, u.password " +
            "FROM customers c JOIN users u ON c.user_id = u.user_id";
    private static final String SQL_UPDATE_CUSTOMER_DETAILS = "UPDATE customers SET address = ?, phone_number = ? WHERE user_id = ?";
    private static final String SQL_DELETE_CUSTOMER = "DELETE FROM customers WHERE customer_id = ?";

    private Scanner scanner;

    public CustomerService(Scanner scanner) {
        this.scanner = scanner;
    }

    public CustomerService() {
        this.scanner = new Scanner(System.in);
    }

    public Customer createCustomer(int userId, String fullName, String email, String password) throws SQLException {
        System.out.print("Enter Customer Address: ");
        String address = scanner.nextLine();
        System.out.print("Enter Customer Phone Number: ");
        String phone = scanner.nextLine();

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SQL_CREATE_CUSTOMER, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, userId);
            pstmt.setString(2, address);
            pstmt.setString(3, phone);

            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        int customerId = rs.getInt(1);
                        return new Customer(userId, fullName, email, password, customerId, address, phone);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Database error creating customer details: " + e.getMessage());
            throw e; // Re-throw to trigger rollback in UserService
        }
        return null;
    }

    public Customer getCustomerByUserId(int userId) {
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SQL_GET_CUSTOMER_BY_USER_ID)) {

            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Customer(
                            userId,
                            rs.getString("full_name"),
                            rs.getString("email"),
                            rs.getString("password"),
                            rs.getInt("customer_id"),
                            rs.getString("address"),
                            rs.getString("phone_number")
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("Database error fetching customer: " + e.getMessage());
        }
        return null;
    }

    public List<Customer> getAllCustomers() {
        List<Customer> customers = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(SQL_GET_ALL_CUSTOMERS)) {

            while (rs.next()) {
                customers.add(new Customer(
                        rs.getInt("user_id"),
                        rs.getString("full_name"),
                        rs.getString("email"),
                        rs.getString("password"),
                        rs.getInt("customer_id"),
                        rs.getString("address"),
                        rs.getString("phone_number")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Database error fetching all customers: " + e.getMessage());
        }
        return customers;
    }

    public boolean updateCustomerDetails(Customer customer) {
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SQL_UPDATE_CUSTOMER_DETAILS)) {

            pstmt.setString(1, customer.getAddress());
            pstmt.setString(2, customer.getPhoneNumber());
            pstmt.setInt(3, customer.getUserId());

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Database error updating customer details: " + e.getMessage());
            return false;
        }
    }

    /**
     * **NEW: Delete Customer**
     * Deletes the customer record from the customers table.
     * NOTE: The corresponding user record should ideally be deleted via UserService in the application logic.
     */
    public boolean deleteCustomer(int customerId) {
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SQL_DELETE_CUSTOMER)) {

            pstmt.setInt(1, customerId);
            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Database error deleting customer: " + e.getMessage());
            return false;
        }
    }
}
