import java.sql.*;
import java.util.Scanner;

public class Main {
    private static final Scanner scanner = new Scanner(System.in);
    private static final UserService userService = new UserService(scanner);

    public static void main(String[] args) {
        System.out.println("====================================");
        System.out.println("Welcome to the Simple Banking System");
        System.out.println("====================================");

        boolean running = true;
        while (running) {
            System.out.println("****** MAIN MENU ******");
            System.out.println("1. Login");
            System.out.println("2. Register as Customer");
            System.out.println("3. Register as Employee");
            System.out.println("4. Exit");
            System.out.print("Enter choice: ");
            String choice = scanner.nextLine();

            switch (choice) {
                case "1":
                    Person loggedInUser = userService.loginUser();
                    if (loggedInUser != null) {
                        new Menu(loggedInUser, scanner).displayMenu();
                    } else {
                        System.out.println("Login failed.");
                    }
                    break;
                case "2":
                    userService.registerUser("CUSTOMER");
                    break;
                case "3":
                    userService.registerUser("EMPLOYEE");
                    break;
                case "4":
                    System.out.println("Exiting application. Goodbye!");
                    running = false;
                    break;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
        scanner.close();
    }
}