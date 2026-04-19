class Employee extends Person {
    private int employeeId;
    private String jobTitle;
    private double salary;

    public Employee(int userId, String fullName, String email, String password, int employeeId, String jobTitle, double salary) {
        super(userId, fullName, email, password);
        this.employeeId = employeeId;
        this.jobTitle = jobTitle;
        this.salary = salary;
    }

    public int getEmployeeId() { return employeeId; }
    public String getJobTitle() { return jobTitle; }
    public double getSalary() { return salary; }

    public void setJobTitle(String jobTitle) { this.jobTitle = jobTitle; }
    public void setSalary(double salary) { this.salary = salary; }

    @Override
    public String toString() {
        return String.format("Employee ID: %d, Name: %s, Title: %s, Salary: $%,.2f",
                employeeId, getFullName(), jobTitle, salary);
    }
}
