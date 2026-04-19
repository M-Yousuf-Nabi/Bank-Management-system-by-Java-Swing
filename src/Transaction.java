import java.sql.Timestamp;

class Transaction {
    private int transactionId;
    private long accountNumber;
    private String type; // DEPOSIT, WITHDRAWAL, TRANSFER_OUT, TRANSFER_IN
    private double amount;
    private String description;
    private Timestamp transactionTime;

    public Transaction(int transactionId, long accountNumber, String type, double amount, String description, Timestamp transactionTime) {
        this.transactionId = transactionId;
        this.accountNumber = accountNumber;
        this.type = type;
        this.amount = amount;
        this.description = description;
        this.transactionTime = transactionTime;
    }

    // Getters
    public String getType() { return type; }
    public double getAmount() { return amount; }
    public Timestamp getTransactionTime() { return transactionTime; }

    @Override
    public String toString() {
        String sign = (type.equals("DEPOSIT") || type.equals("TRANSFER_IN")) ? "+" : "-";
        return String.format("[%s] %s$%,.2f (%s) on %s",
                type, sign, amount, description, transactionTime.toString());
    }
}
