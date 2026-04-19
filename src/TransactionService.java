import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

class TransactionService {
    private static final String SQL_RECORD_TRANSACTION = "INSERT INTO transactions (account_number, transaction_type, amount, description) VALUES (?, ?, ?, ?)";
    private static final String SQL_GET_HISTORY = "SELECT * FROM transactions WHERE account_number = ? ORDER BY transaction_time DESC";

    public void recordTransaction(Connection conn, long accountNumber, String type, double amount, String description) throws SQLException {
        try (PreparedStatement pstmt = conn.prepareStatement(SQL_RECORD_TRANSACTION)) {

            pstmt.setLong(1, accountNumber);
            pstmt.setString(2, type);
            pstmt.setDouble(3, amount);
            pstmt.setString(4, description);

            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Database error recording transaction: " + e.getMessage());
            throw e;
        }
    }

    public List<Transaction> getTransactionHistory(long accountNumber) {
        List<Transaction> history = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SQL_GET_HISTORY)) {

            pstmt.setLong(1, accountNumber);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    history.add(new Transaction(
                            rs.getInt("transaction_id"),
                            rs.getLong("account_number"),
                            rs.getString("transaction_type"),
                            rs.getDouble("amount"),
                            rs.getString("description"),
                            rs.getTimestamp("transaction_time")
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("Database error fetching transaction history: " + e.getMessage());
        }
        return history;
    }
}
