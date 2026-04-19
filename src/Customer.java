class Customer extends Person {
    private int customerId;
    private String address;
    private String phoneNumber;

    public Customer(int userId, String fullName, String email, String password, int customerId, String address, String phoneNumber) {
        super(userId, fullName, email, password);
        this.customerId = customerId;
        this.address = address;
        this.phoneNumber = phoneNumber;
    }

    public int getCustomerId() { return customerId; }
    public String getAddress() { return address; }
    public String getPhoneNumber() { return phoneNumber; }

    public void setAddress(String address) { this.address = address; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    @Override
    public String toString() {
        return String.format("Customer ID: %d, Name: %s, Email: %s, Address: %s",
                customerId, getFullName(), getEmail(), address);
    }
}
