import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

class AccountService {
    private static final String SQL_OPEN_ACCOUNT = "INSERT INTO accounts (account_number, customer_id, account_type, security_pin) VALUES (?, ?, ?, ?)";
    private static final String SQL_GET_ACCOUNTS_BY_CUSTOMER = "SELECT * FROM accounts WHERE customer_id = ?";
    private static final String SQL_GET_ACCOUNT_BY_NUMBER = "SELECT * FROM accounts WHERE account_number = ?";
    private static final String SQL_UPDATE_BALANCE = "UPDATE accounts SET balance = ? WHERE account_number = ?";
    private static final String SQL_UPDATE_PIN = "UPDATE accounts SET security_pin = ? WHERE account_number = ?";
    private static final String SQL_DELETE_ACCOUNT = "DELETE FROM accounts WHERE account_number = ?";

    private Scanner scanner;
    private TransactionService transactionService;

    public AccountService(Scanner scanner) {
        this.scanner = scanner;
        this.transactionService = new TransactionService();
    }

    private long generateAccountNumber() {
        // Generates a 10-digit account number
        return (long) (Math.random() * 9000000000L) + 1000000000L;
    }

    private String generateSecurityPin() {
        // Generates a random 4-digit pin
        return String.format("%04d", new Random().nextInt(10000));
    }

    public boolean openAccount(Customer customer) {
        System.out.println("\n--- Open New Account ---");
        System.out.println("Select Account Type:");
        System.out.println("1. SAVINGS");
        System.out.println("2. FIXED");
        System.out.print("Enter choice (1 or 2): ");
        String typeChoice = scanner.nextLine();
        String accountType = (typeChoice.equals("1") || typeChoice.equalsIgnoreCase("SAVINGS")) ? "SAVINGS" : "FIXED";

        long accountNumber = generateAccountNumber();
        String securityPin = generateSecurityPin();

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SQL_OPEN_ACCOUNT)) {

            pstmt.setLong(1, accountNumber);
            pstmt.setInt(2, customer.getCustomerId());
            pstmt.setString(3, accountType);
            pstmt.setString(4, securityPin);

            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Account opened successfully! 💸");
                System.out.println("Your new Account Number is: " + accountNumber);
                System.out.println("Your Security PIN is: " + securityPin);
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Database error opening account: " + e.getMessage());
        }
        return false;
    }

    public List<Account> getCustomerAccounts(int customerId) {
        List<Account> accounts = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SQL_GET_ACCOUNTS_BY_CUSTOMER)) {

            pstmt.setInt(1, customerId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    accounts.add(new Account(
                            rs.getLong("account_number"),
                            rs.getInt("customer_id"),
                            rs.getString("account_type"),
                            rs.getDouble("balance"),
                            rs.getString("security_pin")
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("Database error fetching customer accounts: " + e.getMessage());
        }
        return accounts;
    }

    public Account getAccountByNumber(long accountNumber) {
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SQL_GET_ACCOUNT_BY_NUMBER)) {

            pstmt.setLong(1, accountNumber);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Account(
                            rs.getLong("account_number"),
                            rs.getInt("customer_id"),
                            rs.getString("account_type"),
                            rs.getDouble("balance"),
                            rs.getString("security_pin")
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("Database error fetching account: " + e.getMessage());
        }
        return null;
    }

    public boolean updateBalance(Account account, Connection conn) throws SQLException {
        try (PreparedStatement pstmt = conn.prepareStatement(SQL_UPDATE_BALANCE)) {
            pstmt.setDouble(1, account.getBalance());
            pstmt.setLong(2, account.getAccountNumber());
            return pstmt.executeUpdate() > 0;
        }
    }

    // NEW Update Method
    public boolean updatePin(Account account, String newPin) {
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SQL_UPDATE_PIN)) {

            account.setSecurityPin(newPin);
            pstmt.setString(1, newPin);
            pstmt.setLong(2, account.getAccountNumber());

            if (pstmt.executeUpdate() > 0) {
                System.out.println("Security PIN updated successfully for account " + account.getAccountNumber());
                return true;
            }
            return false;
        } catch (SQLException e) {
            System.err.println("Database error updating PIN: " + e.getMessage());
            return false;
        }
    }

    // NEW Delete Method
    public boolean deleteAccount(long accountNumber) {
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SQL_DELETE_ACCOUNT)) {

            pstmt.setLong(1, accountNumber);

            if (pstmt.executeUpdate() > 0) {
                System.out.println("Account " + accountNumber + " closed successfully.");
                return true;
            }
            return false;
        } catch (SQLException e) {
            System.err.println("Database error closing account: " + e.getMessage());
            return false;
        }
    }

    public void deposit(Customer customer) {
        Account account = selectAccount(customer);
        if (account == null) return;

        System.out.print("Enter amount to deposit: ");
        double amount = 0;
        try {
            amount = scanner.nextDouble();
        } catch (Exception e) {
            System.out.println("Invalid amount. Returning to menu.");
            scanner.nextLine();
            return;
        }
        scanner.nextLine();

        if (amount <= 0) {
            System.out.println("Deposit amount must be positive.");
            return;
        }

        try (Connection conn = DatabaseConfig.getConnection()) {
            conn.setAutoCommit(false);

            account.setBalance(account.getBalance() + amount);
            if (updateBalance(account, conn)) {
                transactionService.recordTransaction(conn, account.getAccountNumber(), "DEPOSIT", amount, "Cash deposit");
                conn.commit();
                System.out.printf("Deposit successful. New balance: $%,.2f\n", account.getBalance());
            } else {
                conn.rollback();
                System.out.println("Deposit failed: Could not update balance.");
            }
        } catch (SQLException e) {
            System.err.println("Transaction failed (Deposit): " + e.getMessage());
        }
    }

    public void withdraw(Customer customer) {
        Account account = selectAccount(customer);
        if (account == null) return;

        System.out.print("Enter amount to withdraw: ");
        double amount = 0;
        try {
            amount = scanner.nextDouble();
        } catch (Exception e) {
            System.out.println("Invalid amount. Returning to menu.");
            scanner.nextLine();
            return;
        }
        scanner.nextLine();

        System.out.print("Enter your 4-digit PIN: ");
        String pin = scanner.nextLine();

        if (!pin.equals(account.getSecurityPin())) {
            System.out.println("Invalid security PIN. Withdrawal failed.");
            return;
        }

        if (amount <= 0) {
            System.out.println("Withdrawal amount must be positive.");
            return;
        }
        if (amount > account.getBalance()) {
            System.out.println("Insufficient funds. Available balance: $" + account.getBalance());
            return;
        }

        try (Connection conn = DatabaseConfig.getConnection()) {
            conn.setAutoCommit(false);

            account.setBalance(account.getBalance() - amount);
            if (updateBalance(account, conn)) {
                transactionService.recordTransaction(conn, account.getAccountNumber(), "WITHDRAWAL", amount, "Cash withdrawal");
                conn.commit();
                System.out.printf("Withdrawal successful. New balance: $%,.2f\n", account.getBalance());
            } else {
                conn.rollback();
                System.out.println("Withdrawal failed: Could not update balance.");
            }
        } catch (SQLException e) {
            System.err.println("Transaction failed (Withdrawal): " + e.getMessage());
        }
    }

    public void transfer(Customer customer) {
        Account sourceAccount = selectAccount(customer);
        if (sourceAccount == null) return;

        System.out.print("Enter destination Account Number: ");
        long destAccountNumber;
        try {
            destAccountNumber = scanner.nextLong();
            scanner.nextLine();
        } catch (Exception e) {
            System.out.println("Invalid account number format.");
            scanner.nextLine();
            return;
        }

        Account destAccount = getAccountByNumber(destAccountNumber);
        if (destAccount == null) {
            System.out.println("Destination account not found.");
            return;
        }
        if (sourceAccount.getAccountNumber() == destAccountNumber) {
            System.out.println("Cannot transfer to the same account.");
            return;
        }

        System.out.print("Enter amount to transfer: ");
        double amount = 0;
        try {
            amount = scanner.nextDouble();
        } catch (Exception e) {
            System.out.println("Invalid amount. Returning to menu.");
            scanner.nextLine();
            return;
        }
        scanner.nextLine();

        System.out.print("Enter your 4-digit PIN: ");
        String pin = scanner.nextLine();

        if (!pin.equals(sourceAccount.getSecurityPin())) {
            System.out.println("Invalid security PIN. Transfer failed.");
            return;
        }

        if (amount <= 0) {
            System.out.println("Transfer amount must be positive.");
            return;
        }
        if (amount > sourceAccount.getBalance()) {
            System.out.println("Insufficient funds. Available balance: $" + sourceAccount.getBalance());
            return;
        }

        try (Connection conn = DatabaseConfig.getConnection()) {
            conn.setAutoCommit(false);

            sourceAccount.setBalance(sourceAccount.getBalance() - amount);
            boolean sourceUpdated = updateBalance(sourceAccount, conn);

            destAccount.setBalance(destAccount.getBalance() + amount);
            boolean destUpdated = updateBalance(destAccount, conn);

            if (sourceUpdated && destUpdated) {
                transactionService.recordTransaction(conn, sourceAccount.getAccountNumber(), "TRANSFER_OUT", amount, "Transfer to " + destAccountNumber);
                transactionService.recordTransaction(conn, destAccountNumber, "TRANSFER_IN", amount, "Transfer from " + sourceAccount.getAccountNumber());

                conn.commit();
                System.out.printf("Transfer of $%,.2f to account %d successful. 🚀\nNew balance: $%,.2f\n", amount, destAccountNumber, sourceAccount.getBalance());
            } else {
                conn.rollback();
                System.out.println("Transfer failed due to a database error.");
            }
        } catch (SQLException e) {
            System.err.println("Transaction failed (Transfer): " + e.getMessage());
        }
    }

    public void checkBalance(Customer customer) {
        Account account = selectAccount(customer);
        if (account == null) return;

        System.out.printf("Account %d Balance: $%,.2f\n", account.getAccountNumber(), account.getBalance());
    }

    public void viewTransactionHistory(Customer customer) {
        Account account = selectAccount(customer);
        if (account == null) return;

        List<Transaction> history = transactionService.getTransactionHistory(account.getAccountNumber());
        System.out.println("\n--- Transaction History for Account " + account.getAccountNumber() + " ---");
        if (history.isEmpty()) {
            System.out.println("No transactions found.");
            return;
        }

        for (Transaction t : history) {
            System.out.println(t.toString());
        }
    }

    private Account selectAccount(Customer customer) {
        List<Account> accounts = getCustomerAccounts(customer.getCustomerId());
        if (accounts.isEmpty()) {
            System.out.println("No bank accounts found. Please open an account first.");
            return null;
        }

        System.out.println("\n--- Your Accounts ---");
        for (int i = 0; i < accounts.size(); i++) {
            System.out.printf("%d. %s\n", i + 1, accounts.get(i).toString());
        }

        System.out.print("Select account number to proceed: ");
        try {
            long accountNumber = scanner.nextLong();
            scanner.nextLine();

            for (Account acc : accounts) {
                if (acc.getAccountNumber() == accountNumber) {
                    return acc;
                }
            }
            System.out.println("Invalid account selection.");
            return null;
        } catch (Exception e) {
            System.out.println("Invalid input. Please enter a number.");
            scanner.nextLine();
            return null;
        }
    }

    // Helper method to let customer select account
    public Account getAndSelectAccount(Customer customer) {
        return selectAccount(customer);
    }
}
