import java.util.Scanner;
import java.util.List;
import java.util.InputMismatchException;

public class Menu {
    private Person loggedInUser;
    private Scanner scanner;

    private AccountService accountService;
    private UserService userService;
    private CustomerService customerService;
    private EmployeeService employeeService;

    public Menu(Person user, Scanner scanner) {
        this.loggedInUser = user;
        this.scanner = scanner;
        this.accountService = new AccountService(scanner);
        this.userService = new UserService(scanner);
        this.customerService = new CustomerService(scanner);
        this.employeeService = new EmployeeService();
    }

    public void displayMenu() {
        if (loggedInUser instanceof Customer) {
            displayCustomerMenu((Customer) loggedInUser);
        } else if (loggedInUser instanceof Employee) {
            displayEmployeeMenu((Employee) loggedInUser);
        }
    }

    private void displayCustomerMenu(Customer customer) {
        System.out.println("\n--- Welcome, Customer: " + customer.getFullName() + " ---");
        boolean running = true;
        while (running) {
            System.out.println("\nCUSTOMER MENU:");
            System.out.println("1. Deposit Funds");
            System.out.println("2. Withdraw Funds");
            System.out.println("3. Transfer Funds");
            System.out.println("4. Check Account Balance");
            System.out.println("5. Open New Account");
            System.out.println("6. View Transaction History");
            System.out.println("7. Update Profile");
            System.out.println("8. Update Account PIN");
            System.out.println("9. Close/Delete Account");
            System.out.println("10. Delete My Customer Profile");
            System.out.println("11. Logout");
            System.out.print("Enter choice: ");

            String choice = scanner.nextLine();

            try {
                switch (choice) {
                    case "1": accountService.deposit(customer); break;
                    case "2": accountService.withdraw(customer); break;
                    case "3": accountService.transfer(customer); break;
                    case "4": accountService.checkBalance(customer); break;
                    case "5": accountService.openAccount(customer); break;
                    case "6": accountService.viewTransactionHistory(customer); break;
                    case "7": updateCustomerProfile(customer); break;
                    case "8": updateAccountPin(customer); break;
                    case "9": deleteAccount(customer); break;
                    case "10":
                        if (deleteCustomerProfile(customer)) {
                            running = false; // Forced logout after deletion
                        }
                        break;
                    case "11":
                        System.out.println("Logging out...");
                        running = false;
                        break;
                    default:
                        System.out.println("Invalid choice. Please try again.");
                }
            } catch (Exception e) {
                System.err.println("An unexpected error occurred: " + e.getMessage());
            }
        }
    }

    private void displayEmployeeMenu(Employee employee) {
        System.out.println("\n--- Welcome, Employee: " + employee.getFullName() + " (" + employee.getJobTitle() + ") ---");
        boolean running = true;
        while (running) {
            System.out.println("\nEMPLOYEE MENU:");
            System.out.println("1. View All Customers");
            System.out.println("2. View All Employees");
            System.out.println("3. Register New Customer/Employee");
            System.out.println("4. Update My Employee Profile");
            System.out.println("5. Delete My Employee Profile");
            System.out.println("6. Logout");
            System.out.print("Enter choice: ");

            String choice = scanner.nextLine();

            switch (choice) {
                case "1": viewAllCustomers(); break;
                case "2": viewAllEmployees(); break;
                case "3":
                    System.out.println("Register: 1. Customer / 2. Employee");
                    String regChoice = scanner.nextLine();
                    if (regChoice.equals("1")) userService.registerUser("CUSTOMER");
                    else if (regChoice.equals("2")) userService.registerUser("EMPLOYEE");
                    else System.out.println("Invalid registration choice.");
                    break;
                case "4": updateEmployeeProfile(employee); break;
                case "5":
                    if (deleteEmployeeProfile(employee)) {
                        running = false;
                    }
                    break;
                case "6":
                    System.out.println("Logging out...");
                    running = false;
                    break;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    private void updateCustomerProfile(Customer customer) {
        System.out.println("\n--- Update Customer Profile ---");
        System.out.println("Current Details:");
        System.out.println("1. Full Name: " + customer.getFullName());
        System.out.println("2. Email: " + customer.getEmail());
        System.out.println("3. Password (Hidden)");
        System.out.println("4. Address: " + customer.getAddress());
        System.out.println("5. Phone: " + customer.getPhoneNumber());
        System.out.println("6. Cancel");
        System.out.print("Select field to update: ");
        String fieldChoice = scanner.nextLine();

        boolean updatedUser = false;
        boolean updatedCustomer = false;

        try {
            switch (fieldChoice) {
                case "1":
                    System.out.print("Enter New Full Name: ");
                    customer.setFullName(scanner.nextLine());
                    updatedUser = userService.updateUser(customer);
                    break;
                case "2":
                    System.out.print("Enter New Email: ");
                    String newEmail = scanner.nextLine();
                    if (!userService.checkEmailExists(newEmail) || newEmail.equals(customer.getEmail())) {
                        customer.setEmail(newEmail);
                        updatedUser = userService.updateUser(customer);
                    } else {
                        System.out.println("Email already in use. Update cancelled.");
                    }
                    break;
                case "3":
                    System.out.print("Enter New Password: ");
                    customer.setPassword(scanner.nextLine());
                    updatedUser = userService.updateUser(customer);
                    break;
                case "4":
                    System.out.print("Enter New Address: ");
                    customer.setAddress(scanner.nextLine());
                    updatedCustomer = customerService.updateCustomerDetails(customer);
                    break;
                case "5":
                    System.out.print("Enter New Phone Number: ");
                    customer.setPhoneNumber(scanner.nextLine());
                    updatedCustomer = customerService.updateCustomerDetails(customer);
                    break;
                case "6": return;
                default: System.out.println("Invalid choice."); return;
            }

            if (updatedUser || updatedCustomer) {
                System.out.println("Profile updated successfully!");
            } else {
                System.out.println("Profile update failed or no changes were necessary.");
            }
        } catch (Exception e) {
            System.err.println("Update failed: " + e.getMessage());
        }
    }

    private void updateAccountPin(Customer customer) {
        Account account = accountService.getAndSelectAccount(customer);
        if (account == null) return;

        System.out.print("Enter new 4-digit PIN: ");
        String newPin = scanner.nextLine();

        if (newPin.length() != 4 || !newPin.matches("\\d{4}")) {
            System.out.println("PIN must be exactly 4 digits.");
            return;
        }

        accountService.updatePin(account, newPin);
    }

    private void deleteAccount(Customer customer) {
        Account account = accountService.getAndSelectAccount(customer);
        if (account == null) return;

        // REQUIRED STEP: Check if the account is empty
        if (account.getBalance() != 0.0) {
            System.out.printf("Cannot close account %d. Balance must be zero ($%,.2f).\n",
                    account.getAccountNumber(), account.getBalance());
            return;
        }

        System.out.println("\n!!!WARNING: ACCOUNT CLOSURE !!!");
        System.out.println("You are about to permanently close account " + account.getAccountNumber() + ".");
        System.out.println("This action is irreversible and will delete all its transaction history.");

        System.out.print("To confirm closure, please type the word 'DELETE': ");
        if (scanner.nextLine().equals("DELETE")) {
            if (accountService.deleteAccount(account.getAccountNumber())) {
                System.out.println("Account " + account.getAccountNumber() + " has been successfully closed and deleted.");
            } else {
                System.out.println("Account closure failed due to a database error.");
            }
        } else {
            System.out.println("Account closure cancelled.");
        }
    }

    private boolean deleteCustomerProfile(Customer customer) {
        System.out.println("\n!!!WARNING: PERMANENT DELETION !!!");
        System.out.println("This action is irreversible and will remove ALL your data:");
        System.out.println("  - Customer Profile and User Login");
        System.out.println("  - All Bank Accounts");
        System.out.println("  - All Transaction History");

        System.out.print("To confirm permanent deletion, please enter your Email address ('" + customer.getEmail() + "'): ");
        String emailConfirm = scanner.nextLine();

        if (emailConfirm.equals(customer.getEmail())) {
            System.out.println("Processing deletion...");

            //Delete customer-specific record
            customerService.deleteCustomer(customer.getCustomerId());

            //Delete main user record
            if (userService.deleteUser(customer.getUserId())) {
                System.out.println("Your customer profile has been permanently deleted. You are now logged out.");
                return true;
            } else {
                System.out.println("Deletion failed due to a database error.");
                return false;
            }
        } else {
            System.out.println("Email mismatch. Profile deletion cancelled.");
            return false;
        }
    }

    private void viewAllCustomers() {
        List<Customer> customers = customerService.getAllCustomers();
        System.out.println("\n--- All Customers ---");
        if (customers.isEmpty()) {
            System.out.println("No customers found.");
            return;
        }
        for (Customer c : customers) {
            System.out.println(c.toString());
        }
    }

    private void viewAllEmployees() {
        List<Employee> employees = employeeService.getAllEmployees();
        System.out.println("\n--- All Employees ---");
        if (employees.isEmpty()) {
            System.out.println("No employees found.");
            return;
        }
        for (Employee e : employees) {
            System.out.println(e.toString());
        }
    }

    private void updateEmployeeProfile(Employee employee) {
        System.out.println("\n--- Update Employee Profile ---");
        System.out.println("Current Details:");
        System.out.println("1. Full Name: " + employee.getFullName());
        System.out.println("2. Email: " + employee.getEmail());
        System.out.println("3. Password (Hidden)");
        System.out.println("4. Job Title: " + employee.getJobTitle());
        System.out.printf("5. Salary: $%,.2f\n", employee.getSalary());
        System.out.println("6. Cancel");
        System.out.print("Select field to update: ");
        String fieldChoice = scanner.nextLine();

        boolean updatedUser = false;
        boolean updatedEmployee = false;

        try {
            switch (fieldChoice) {
                case "1":
                    System.out.print("Enter New Full Name: ");
                    employee.setFullName(scanner.nextLine());
                    updatedUser = userService.updateUser(employee);
                    break;
                case "2":
                    System.out.print("Enter New Email: ");
                    String newEmail = scanner.nextLine();
                    if (!userService.checkEmailExists(newEmail) || newEmail.equals(employee.getEmail())) {
                        employee.setEmail(newEmail);
                        updatedUser = userService.updateUser(employee);
                    } else {
                        System.out.println("Email already in use. Update cancelled.");
                    }
                    break;
                case "3":
                    System.out.print("Enter New Password: ");
                    employee.setPassword(scanner.nextLine());
                    updatedUser = userService.updateUser(employee);
                    break;
                case "4":
                    System.out.print("Enter New Job Title: ");
                    employee.setJobTitle(scanner.nextLine());
                    updatedEmployee = employeeService.updateEmployeeDetails(employee);
                    break;
                case "5":
                    System.out.print("Enter New Salary: ");
                    double newSalary = scanner.nextDouble();
                    scanner.nextLine();
                    employee.setSalary(newSalary);
                    updatedEmployee = employeeService.updateEmployeeDetails(employee);
                    break;
                case "6": return;
                default: System.out.println("Invalid choice."); return;
            }

            if (updatedUser || updatedEmployee) {
                System.out.println("Profile updated successfully!");
            } else {
                System.out.println("Profile update failed or no changes were made.");
            }
        } catch (InputMismatchException e) {
            System.out.println("Invalid input format for salary. Update failed.");
            scanner.nextLine(); // Clear buffer
        } catch (Exception e) {
            System.err.println("Update failed: " + e.getMessage());
        }
    }

    private boolean deleteEmployeeProfile(Employee employee) {
        System.out.println("\n!!! WARNING: PERMANENT DELETION !!!");
        System.out.println("This action is irreversible and will remove your employee record and user login.");

        System.out.print("To confirm permanent deletion, please enter your Email address ('" + employee.getEmail() + "'): ");
        String emailConfirm = scanner.nextLine();

        if (emailConfirm.equals(employee.getEmail())) {
            System.out.println("Processing deletion...");

            //Delete employee-specific record
            employeeService.deleteEmployee(employee.getEmployeeId());

            //Delete main user record
            if (userService.deleteUser(employee.getUserId())) {
                System.out.println("Your employee profile has been permanently deleted. You are now logged out.");
                return true;
            } else {
                System.out.println("Deletion failed due to a database error.");
                return false;
            }
        } else {
            System.out.println("Email mismatch. Profile deletion cancelled.");
            return false;
        }
    }
}