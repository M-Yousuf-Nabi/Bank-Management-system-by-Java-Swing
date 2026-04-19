import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Random;

/**
 * UI-friendly wrapper for account operations used by the Swing UI.
 * Delegates DB updates to AccountService and TransactionService so business logic remains centralized.
 *
 * No interactive Scanner usage in these methods: UI provides inputs as parameters.
 */
class AccountOperationsService {
    private final AccountService accountService = new AccountService(new java.util.Scanner(System.in));
    private final TransactionService transactionService = new TransactionService();

    private static final String SQL_OPEN_ACCOUNT = "INSERT INTO accounts (account_number, customer_id, account_type, security_pin) VALUES (?, ?, ?, ?)";

    public List<Account> getAccountsForCustomer(int customerId) {
        return accountService.getCustomerAccounts(customerId);
    }

    public Account getAccount(long accountNumber) {
        return accountService.getAccountByNumber(accountNumber);
    }

    public static long generateAccountNumber() {
        return (long) (Math.random() * 9000000000L) + 1000000000L;
    }

    public static String generatePin() {
        return String.format("%04d", new Random().nextInt(10000));
    }

    public Account openAccount(int customerId, String accountType) throws SQLException {
        long accountNumber = generateAccountNumber();
        String pin = generatePin();

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SQL_OPEN_ACCOUNT)) {
            pstmt.setLong(1, accountNumber);
            pstmt.setInt(2, customerId);
            pstmt.setString(3, accountType);
            pstmt.setString(4, pin);

            if (pstmt.executeUpdate() > 0) {
                return new Account(accountNumber, customerId, accountType, 0.0, pin);
            } else {
                return null;
            }
        }
    }

    public Account deposit(long accountNumber, double amount) throws SQLException {
        if (amount <= 0) throw new IllegalArgumentException("Amount must be positive.");
        Account account = getAccount(accountNumber);
        if (account == null) throw new IllegalArgumentException("Account not found.");

        try (Connection conn = DatabaseConfig.getConnection()) {
            conn.setAutoCommit(false);
            account.setBalance(account.getBalance() + amount);
            boolean ok = accountService.updateBalance(account, conn);
            if (!ok) {
                conn.rollback();
                throw new SQLException("Failed to update balance.");
            }
            transactionService.recordTransaction(conn, accountNumber, "DEPOSIT", amount, "Cash deposit");
            conn.commit();
            return account;
        }
    }

    public Account withdraw(long accountNumber, double amount, String pin) throws SQLException {
        if (amount <= 0) throw new IllegalArgumentException("Amount must be positive.");
        Account account = getAccount(accountNumber);
        if (account == null) throw new IllegalArgumentException("Account not found.");
        if (!account.getSecurityPin().equals(pin)) throw new SecurityException("Invalid pin.");
        if (amount > account.getBalance()) throw new IllegalStateException("Insufficient balance.");

        try (Connection conn = DatabaseConfig.getConnection()) {
            conn.setAutoCommit(false);
            account.setBalance(account.getBalance() - amount);
            boolean ok = accountService.updateBalance(account, conn);
            if (!ok) {
                conn.rollback();
                throw new SQLException("Failed to update balance.");
            }
            transactionService.recordTransaction(conn, accountNumber, "WITHDRAWAL", amount, "Cash withdrawal");
            conn.commit();
            return account;
        }
    }

    public void transfer(long sourceAccountNumber, long destAccountNumber, double amount, String pin) throws SQLException {
        if (sourceAccountNumber == destAccountNumber) throw new IllegalArgumentException("Cannot transfer to same account.");
        if (amount <= 0) throw new IllegalArgumentException("Amount must be positive.");

        Account source = getAccount(sourceAccountNumber);
        if (source == null) throw new IllegalArgumentException("Source account not found.");
        if (!source.getSecurityPin().equals(pin)) throw new SecurityException("Invalid pin.");
        if (amount > source.getBalance()) throw new IllegalStateException("Insufficient balance.");

        Account dest = getAccount(destAccountNumber);
        if (dest == null) throw new IllegalArgumentException("Destination account not found.");

        try (Connection conn = DatabaseConfig.getConnection()) {
            conn.setAutoCommit(false);

            source.setBalance(source.getBalance() - amount);
            boolean srcOk = accountService.updateBalance(source, conn);

            dest.setBalance(dest.getBalance() + amount);
            boolean dstOk = accountService.updateBalance(dest, conn);

            if (!srcOk || !dstOk) {
                conn.rollback();
                throw new SQLException("Failed to update balances.");
            }

            transactionService.recordTransaction(conn, sourceAccountNumber, "TRANSFER_OUT", amount, "Transfer to " + destAccountNumber);
            transactionService.recordTransaction(conn, destAccountNumber, "TRANSFER_IN", amount, "Transfer from " + sourceAccountNumber);

            conn.commit();
        }
    }

    public boolean updatePin(long accountNumber, String newPin) {
        Account acc = getAccount(accountNumber);
        if (acc == null) throw new IllegalArgumentException("Account not found.");
        if (!newPin.matches("\\d{4}")) throw new IllegalArgumentException("PIN must be 4 digits.");
        return accountService.updatePin(acc, newPin);
    }

    public boolean closeAccount(long accountNumber) {
        Account acc = getAccount(accountNumber);
        if (acc == null) throw new IllegalArgumentException("Account not found.");
        if (Math.abs(acc.getBalance()) > 0.000001) {
            throw new IllegalStateException("Balance must be zero to close account.");
        }
        return accountService.deleteAccount(accountNumber);
    }
}