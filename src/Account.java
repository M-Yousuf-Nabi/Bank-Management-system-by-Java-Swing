class Account {
    private long accountNumber;
    private int customerId;
    private String accountType; // SAVINGS or FIXED
    private double balance;
    private String securityPin; // Stored plain for simplicity

    public Account(long accountNumber, int customerId, String accountType, double balance, String securityPin) {
        this.accountNumber = accountNumber;
        this.customerId = customerId;
        this.accountType = accountType;
        this.balance = balance;
        this.securityPin = securityPin;
    }

    // Getters
    public long getAccountNumber() { return accountNumber; }
    public int getCustomerId() { return customerId; }
    public String getAccountType() { return accountType; }
    public double getBalance() { return balance; }
    public String getSecurityPin() { return securityPin; }

    public void setBalance(double balance) { this.balance = balance; }
    public void setSecurityPin(String securityPin) { this.securityPin = securityPin; }

    @Override
    public String toString() {
        return String.format("Account No: %d, Type: %s, Balance: $%,.2f",
                accountNumber, accountType, balance);
    }
}
