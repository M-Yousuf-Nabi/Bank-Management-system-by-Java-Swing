import java.sql.*;
import java.util.ArrayList;
import java.util.List;

class EmployeeService {
    // --------------------------------------------------------
    // SQL for Employee Class
    // --------------------------------------------------------
    private static final String SQL_CREATE_EMPLOYEE = "INSERT INTO employees (user_id, job_title, salary) VALUES (?, ?, ?)";
    private static final String SQL_GET_EMPLOYEE_BY_USER_ID = "SELECT e.employee_id, e.job_title, e.salary, u.full_name, u.email, u.password " +
            "FROM employees e JOIN users u ON e.user_id = u.user_id WHERE e.user_id = ?";
    private static final String SQL_GET_ALL_EMPLOYEES = "SELECT e.employee_id, e.job_title, e.salary, u.user_id, u.full_name, u.email, u.password " +
            "FROM employees e JOIN users u ON e.user_id = u.user_id";
    // **NEW SQL for Update/Delete**
    private static final String SQL_UPDATE_EMPLOYEE_DETAILS = "UPDATE employees SET job_title = ?, salary = ? WHERE user_id = ?";
    private static final String SQL_DELETE_EMPLOYEE = "DELETE FROM employees WHERE employee_id = ?";


    public Employee createEmployee(int userId, String fullName, String email, String password) throws SQLException {
        // Dummy data for employee details
        String jobTitle = "Banking Officer";
        double salary = 60000.00;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SQL_CREATE_EMPLOYEE, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, userId);
            pstmt.setString(2, jobTitle);
            pstmt.setDouble(3, salary);

            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        int employeeId = rs.getInt(1);
                        return new Employee(userId, fullName, email, password, employeeId, jobTitle, salary);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Database error creating employee details: " + e.getMessage());
            throw e; // Re-throw to trigger rollback in UserService
        }
        return null;
    }

    public Employee getEmployeeByUserId(int userId) {
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SQL_GET_EMPLOYEE_BY_USER_ID)) {

            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Employee(
                            userId,
                            rs.getString("full_name"),
                            rs.getString("email"),
                            rs.getString("password"),
                            rs.getInt("employee_id"),
                            rs.getString("job_title"),
                            rs.getDouble("salary")
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("Database error fetching employee: " + e.getMessage());
        }
        return null;
    }

    public List<Employee> getAllEmployees() {
        List<Employee> employees = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(SQL_GET_ALL_EMPLOYEES)) {

            while (rs.next()) {
                employees.add(new Employee(
                        rs.getInt("user_id"),
                        rs.getString("full_name"),
                        rs.getString("email"),
                        rs.getString("password"),
                        rs.getInt("employee_id"),
                        rs.getString("job_title"),
                        rs.getDouble("salary")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Database error fetching all employees: " + e.getMessage());
        }
        return employees;
    }

    /**
     * **NEW: Update Employee Details**
     * Updates the employee-specific details (job title, salary).
     */
    public boolean updateEmployeeDetails(Employee employee) {
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SQL_UPDATE_EMPLOYEE_DETAILS)) {

            pstmt.setString(1, employee.getJobTitle());
            pstmt.setDouble(2, employee.getSalary());
            pstmt.setInt(3, employee.getUserId());

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Database error updating employee details: " + e.getMessage());
            return false;
        }
    }

    /**
     * **NEW: Delete Employee**
     * Deletes the employee record from the employees table.
     * NOTE: The corresponding user record should ideally be deleted via UserService in the application logic.
     */
    public boolean deleteEmployee(int employeeId) {
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SQL_DELETE_EMPLOYEE)) {

            pstmt.setInt(1, employeeId);
            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Database error deleting employee: " + e.getMessage());
            return false;
        }
    }
}
