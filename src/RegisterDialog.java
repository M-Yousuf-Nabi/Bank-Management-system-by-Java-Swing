import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.sql.SQLException;

/**
 * RegisterDialog - professional form layout, larger spacing, consistent fonts and buttons.
 * Includes email, phone, and password validation.
 */
class RegisterDialog extends JDialog {
    private final JComboBox<String> roleBox = new JComboBox<>(new String[]{"CUSTOMER", "EMPLOYEE"});
    private final JTextField nameField = new JTextField();
    private final JTextField emailField = new JTextField();
    private final JPasswordField passwordField = new JPasswordField();

    // Customer fields
    private final JTextField addressField = new JTextField();
    private final JTextField phoneField = new JTextField();

    // Employee fields
    private final JTextField jobTitleField = new JTextField();
    private final JTextField salaryField = new JTextField();

    private final JButton submitBtn = new JButton("Create");
    private final JButton cancelBtn = new JButton("Cancel");

    private final AuthService authService = new AuthService();
    private final UserService userService = new UserService(new java.util.Scanner(System.in));

    private Person createdPerson;

    RegisterDialog(JFrame parent) {
        super(parent, "Register", true);
        setSize(1000, 700);
        setMinimumSize(new Dimension(900, 600));
        setLocationRelativeTo(parent);

        Font base = UIManager.getFont("Label.font");
        Font labelFont = base.deriveFont(base.getSize() + 3f);
        Font fieldFont = UIManager.getFont("TextField.font").deriveFont(base.getSize() + 2f);
        Font btnFont = UIManager.getFont("Button.font").deriveFont(base.getSize() + 2f);

        JPanel main = new JPanel(new BorderLayout());
        main.setBorder(new EmptyBorder(18, 18, 18, 18));

        JLabel header = new JLabel("Create New Account");
        header.setFont(labelFont.deriveFont(Font.BOLD, labelFont.getSize() + 2f));
        header.setBorder(new EmptyBorder(6, 6, 12, 6));
        main.add(header, BorderLayout.NORTH);

        JPanel formArea = new JPanel(new GridBagLayout());
        formArea.setBorder(new TitledBorder("Registration Details"));
        ((TitledBorder) formArea.getBorder()).setTitleFont(labelFont.deriveFont(Font.BOLD));
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(12, 12, 12, 12);
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.anchor = GridBagConstraints.WEST;

        int row = 0;

        // Role
        gc.gridx = 0; gc.gridy = row;
        JLabel roleLabel = new JLabel("Role:"); roleLabel.setFont(labelFont);
        formArea.add(roleLabel, gc);
        roleBox.setFont(fieldFont);
        roleBox.setPreferredSize(new Dimension(240, 36));
        gc.gridx = 1; formArea.add(roleBox, gc); row++;

        // Name
        gc.gridx = 0; gc.gridy = row;
        JLabel nameLabel = new JLabel("Full Name:"); nameLabel.setFont(labelFont);
        formArea.add(nameLabel, gc);
        nameField.setFont(fieldFont);
        nameField.setColumns(35);
        gc.gridx = 1; formArea.add(nameField, gc); row++;

        // Email
        gc.gridx = 0; gc.gridy = row;
        JLabel emailLabel = new JLabel("Email:"); emailLabel.setFont(labelFont);
        formArea.add(emailLabel, gc);
        emailField.setFont(fieldFont);
        emailField.setColumns(35);
        gc.gridx = 1; formArea.add(emailField, gc); row++;

        // Password
        gc.gridx = 0; gc.gridy = row;
        JLabel pwdLabel = new JLabel("Password:"); pwdLabel.setFont(labelFont);
        formArea.add(pwdLabel, gc);
        passwordField.setFont(fieldFont);
        passwordField.setColumns(30);
        gc.gridx = 1; formArea.add(passwordField, gc); row++;

        // Customer: Address
        gc.gridx = 0; gc.gridy = row;
        JLabel addrLabel = new JLabel("Address (Customer):"); addrLabel.setFont(labelFont);
        formArea.add(addrLabel, gc);
        addressField.setFont(fieldFont);
        addressField.setColumns(35);
        gc.gridx = 1; formArea.add(addressField, gc); row++;

        // Customer: Phone
        gc.gridx = 0; gc.gridy = row;
        JLabel phoneLabel = new JLabel("Phone (Customer):"); phoneLabel.setFont(labelFont);
        formArea.add(phoneLabel, gc);
        phoneField.setFont(fieldFont);
        phoneField.setColumns(25);
        gc.gridx = 1; formArea.add(phoneField, gc); row++;

        // Employee: Job Title
        gc.gridx = 0; gc.gridy = row;
        JLabel jobLabel = new JLabel("Job Title (Employee):"); jobLabel.setFont(labelFont);
        formArea.add(jobLabel, gc);
        jobTitleField.setFont(fieldFont);
        jobTitleField.setColumns(30);
        gc.gridx = 1; formArea.add(jobTitleField, gc); row++;

        // Employee: Salary
        gc.gridx = 0; gc.gridy = row;
        JLabel salLabel = new JLabel("Salary (Employee):"); salLabel.setFont(labelFont);
        formArea.add(salLabel, gc);
        salaryField.setFont(fieldFont);
        salaryField.setColumns(20);
        gc.gridx = 1; formArea.add(salaryField, gc); row++;

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 18, 12));
        actions.setBorder(new EmptyBorder(8, 8, 8, 8));
        submitBtn.setFont(btnFont);
        cancelBtn.setFont(btnFont);
        submitBtn.setPreferredSize(new Dimension(140, 44));
        cancelBtn.setPreferredSize(new Dimension(140, 44));
        actions.add(cancelBtn);
        actions.add(submitBtn);

        main.add(formArea, BorderLayout.CENTER);
        main.add(actions, BorderLayout.SOUTH);

        add(main);

        roleBox.addActionListener(e -> toggleRoleFields());
        toggleRoleFields();

        cancelBtn.addActionListener(e -> dispose());
        submitBtn.addActionListener(e -> doRegister());
    }

    private void toggleRoleFields() {
        boolean isCustomer = "CUSTOMER".equals(roleBox.getSelectedItem());
        addressField.setEnabled(isCustomer);
        phoneField.setEnabled(isCustomer);
        jobTitleField.setEnabled(!isCustomer);
        salaryField.setEnabled(!isCustomer);
    }

    private void doRegister() {
        String role = (String) roleBox.getSelectedItem();
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String pwd = new String(passwordField.getPassword());

        // Required fields
        if (name.isEmpty() || email.isEmpty() || pwd.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Name, email, and password are required.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Email validation
        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            JOptionPane.showMessageDialog(this,
                    "Invalid email format. Example: name@example.com",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Password validation (≥6 characters)
        if (pwd.length() < 6) {
            JOptionPane.showMessageDialog(this,
                    "Password must be at least 6 characters long.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Check email already registered
        if (new UserService(new java.util.Scanner(System.in)).checkEmailExists(email)) {
            JOptionPane.showMessageDialog(this, "Email already registered.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            if ("CUSTOMER".equals(role)) {
                String addr = addressField.getText().trim();
                String phone = phoneField.getText().trim();

                if (addr.isEmpty() || phone.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Address and phone are required for customers.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Phone validation (11 digits only)
                if (!phone.matches("\\d{11}")) {
                    JOptionPane.showMessageDialog(this,
                            "Phone number must be exactly 11 digits.",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }

                Customer c = authService.registerCustomer(name, email, pwd, addr, phone);
                if (c == null) {
                    JOptionPane.showMessageDialog(this, "Registration failed.", "Error", JOptionPane.ERROR_MESSAGE);
                } else {
                    createdPerson = c;
                    JOptionPane.showMessageDialog(this, "Customer registered successfully.");
                    dispose();
                }
            } else {
                String title = jobTitleField.getText().trim();

                if (title.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Job title is required for employees.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                double salary;
                try {
                    salary = Double.parseDouble(salaryField.getText().trim());
                } catch (NumberFormatException nfe) {
                    JOptionPane.showMessageDialog(this, "Invalid salary.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                Employee e = authService.registerEmployee(name, email, pwd, title, salary);
                if (e == null) {
                    JOptionPane.showMessageDialog(this, "Registration failed.", "Error", JOptionPane.ERROR_MESSAGE);
                } else {
                    createdPerson = e;
                    JOptionPane.showMessageDialog(this, "Employee registered successfully.");
                    dispose();
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public Person getCreatedPerson() {
        return createdPerson;
    }
}
