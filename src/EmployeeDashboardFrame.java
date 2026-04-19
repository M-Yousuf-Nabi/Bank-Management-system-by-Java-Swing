import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * EmployeeDashboardFrame - more professional layout, spacing, fonts, tables and buttons.
 * Opens maximized automatically. Pop-ups use larger dialogs when required.
 */
class EmployeeDashboardFrame extends JFrame {
    private final Employee employee;
    private final LoginFrame loginFrame;

    private final UserService userService = new UserService(new java.util.Scanner(System.in));
    private final EmployeeService employeeService = new EmployeeService();
    private final CustomerService customerService = new CustomerService();

    // Tables
    private final DefaultTableModel customersModel = new DefaultTableModel(new Object[]{"Customer ID", "Name", "Email", "Address", "Phone"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final DefaultTableModel employeesModel = new DefaultTableModel(new Object[]{"Employee ID", "Name", "Email", "Title", "Salary"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };

    // Profile fields
    private final JTextField nameField = new JTextField();
    private final JTextField emailField = new JTextField();
    private final JPasswordField passwordField = new JPasswordField();
    private final JTextField titleField = new JTextField();
    private final JTextField salaryField = new JTextField();

    private final JLabel statusLabel = new JLabel(" ");

    EmployeeDashboardFrame(Employee employee, LoginFrame loginFrame) {
        super("Banking System - Employee Dashboard");
        this.employee = employee;
        this.loginFrame = loginFrame;

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 650);
        setMinimumSize(new Dimension(1000, 650));
        setLocationRelativeTo(null);

        // Fonts and colors
        Font base = UIManager.getFont("Label.font");
        Font titleFont = base.deriveFont(base.getSize() + 4f).deriveFont(Font.BOLD);
        Font labelFont = base.deriveFont(base.getSize() + 3f);
        Font fieldFont = UIManager.getFont("TextField.font").deriveFont(base.getSize() + 2f);
        Font btnFont = UIManager.getFont("Button.font").deriveFont(base.getSize() + 2f);
        Color headerBg = new Color(33, 150, 243);
        Color headerFg = Color.WHITE;

        // Header toolbar
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(headerBg);
        header.setBorder(new EmptyBorder(12, 18, 12, 18));
        JLabel title = new JLabel("Employee Dashboard - " + employee.getFullName());
        title.setForeground(headerFg);
        title.setFont(titleFont);
        header.add(title, BorderLayout.WEST);

        JButton logoutBtn = new JButton("Logout");
        logoutBtn.setFont(btnFont);
        logoutBtn.setPreferredSize(new Dimension(140, 40));
        header.add(logoutBtn, BorderLayout.EAST);
        logoutBtn.addActionListener(e -> {
            dispose();
            loginFrame.backToLogin();
        });

        // Main panel
        JPanel main = new JPanel(new BorderLayout(12, 12));
        main.setBorder(new EmptyBorder(14, 14, 14, 14));

        // Top action row
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 10));
        JButton refreshBtn = new JButton("Refresh");
        JButton registerBtn = new JButton("Register New");
        refreshBtn.setFont(btnFont); registerBtn.setFont(btnFont);
        refreshBtn.setPreferredSize(new Dimension(140, 40));
        registerBtn.setPreferredSize(new Dimension(160, 40));
        top.add(refreshBtn); top.add(registerBtn);

        // Split pane with customers (top) and employees (bottom)
        JTable customersTable = new JTable(customersModel);
        customersTable.setRowHeight(28);
        customersTable.setFont(customersTable.getFont().deriveFont(customersTable.getFont().getSize() + 2f));
        JTable employeesTable = new JTable(employeesModel);
        employeesTable.setRowHeight(28);
        employeesTable.setFont(employeesTable.getFont().deriveFont(employeesTable.getFont().getSize() + 2f));

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                new JScrollPane(customersTable),
                new JScrollPane(employeesTable));
        split.setDividerLocation(320);

        // Profile panel
        JPanel profilePanel = new JPanel(new GridBagLayout());
        profilePanel.setBorder(new TitledBorder("My Profile"));
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(12, 12, 12, 12);
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.anchor = GridBagConstraints.WEST;

        int row = 0;
        JLabel nameLbl = new JLabel("Full Name:"); nameLbl.setFont(labelFont);
        gc.gridx = 0; gc.gridy = row; profilePanel.add(nameLbl, gc);
        nameField.setFont(fieldFont); nameField.setColumns(30);
        gc.gridx = 1; gc.gridy = row++; profilePanel.add(nameField, gc);

        JLabel emailLbl = new JLabel("Email:"); emailLbl.setFont(labelFont);
        gc.gridx = 0; gc.gridy = row; profilePanel.add(emailLbl, gc);
        emailField.setFont(fieldFont); emailField.setColumns(30);
        gc.gridx = 1; gc.gridy = row++; profilePanel.add(emailField, gc);

        JLabel passLbl = new JLabel("Password:"); passLbl.setFont(labelFont);
        gc.gridx = 0; gc.gridy = row; profilePanel.add(passLbl, gc);
        passwordField.setFont(fieldFont); passwordField.setColumns(28);
        gc.gridx = 1; gc.gridy = row++; profilePanel.add(passwordField, gc);

        JLabel titleLbl = new JLabel("Job Title:"); titleLbl.setFont(labelFont);
        gc.gridx = 0; gc.gridy = row; profilePanel.add(titleLbl, gc);
        titleField.setFont(fieldFont); titleField.setColumns(30);
        gc.gridx = 1; gc.gridy = row++; profilePanel.add(titleField, gc);

        JLabel salLbl = new JLabel("Salary:"); salLbl.setFont(labelFont);
        gc.gridx = 0; gc.gridy = row; profilePanel.add(salLbl, gc);
        salaryField.setFont(fieldFont); salaryField.setColumns(18);
        gc.gridx = 1; gc.gridy = row++; profilePanel.add(salaryField, gc);

        JPanel profileActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 8));
        JButton deleteBtn = new JButton("Delete My Profile");
        JButton saveBtn = new JButton("Save");
        deleteBtn.setPreferredSize(new Dimension(160, 40));
        saveBtn.setPreferredSize(new Dimension(140, 40));
        deleteBtn.setFont(btnFont); saveBtn.setFont(btnFont);
        profileActions.add(deleteBtn); profileActions.add(saveBtn);
        gc.gridx = 0; gc.gridy = row; gc.gridwidth = 2;
        profilePanel.add(profileActions, gc);

        // Compose center area: left big split and right profile panel
        JSplitPane mainSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, split, profilePanel);
        mainSplit.setResizeWeight(0.65);

        main.add(top, BorderLayout.NORTH);
        main.add(mainSplit, BorderLayout.CENTER);

        // Status bar
        JPanel statusBar = new JPanel(new BorderLayout());
        statusBar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(220,220,220)));
        statusLabel.setFont(UIManager.getFont("Label.font").deriveFont(12f));
        statusLabel.setBorder(new EmptyBorder(6, 10, 6, 10));
        statusBar.add(statusLabel, BorderLayout.WEST);

        // Assemble frame
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(header, BorderLayout.NORTH);
        getContentPane().add(main, BorderLayout.CENTER);
        getContentPane().add(statusBar, BorderLayout.SOUTH);

        // Load content
        loadPeople();
        loadProfile();

        // Wire actions (logic unchanged)
        refreshBtn.addActionListener(e -> loadPeople());
        registerBtn.addActionListener(e -> openRegister());
        saveBtn.addActionListener(e -> saveProfile());
        deleteBtn.addActionListener(e -> deleteProfile());

        // Open maximized
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setVisible(true);
    }

    private void setStatus(String msg) {
        statusLabel.setText(msg);
    }

    private void loadPeople() {
        customersModel.setRowCount(0);
        List<Customer> customers = customerService.getAllCustomers();
        for (Customer c : customers) {
            customersModel.addRow(new Object[]{c.getCustomerId(), c.getFullName(), c.getEmail(), c.getAddress(), c.getPhoneNumber()});
        }

        employeesModel.setRowCount(0);
        List<Employee> employees = employeeService.getAllEmployees();
        for (Employee e : employees) {
            employeesModel.addRow(new Object[]{e.getEmployeeId(), e.getFullName(), e.getEmail(), e.getJobTitle(), e.getSalary()});
        }
        setStatus("Loaded " + customers.size() + " customers and " + employees.size() + " employees.");
    }

    private void openRegister() {
        // RegisterDialog already sized large; center relative to this frame
        RegisterDialog dlg = new RegisterDialog(this);
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);
        if (dlg.getCreatedPerson() != null) {
            loadPeople();
        }
    }

    private void loadProfile() {
        nameField.setText(employee.getFullName());
        emailField.setText(employee.getEmail());
        passwordField.setText(employee.getPassword());
        titleField.setText(employee.getJobTitle());
        salaryField.setText(String.valueOf(employee.getSalary()));
    }

    private void saveProfile() {
        String newName = nameField.getText().trim();
        String newEmail = emailField.getText().trim();
        String newPass = new String(passwordField.getPassword());
        String newTitle = titleField.getText().trim();
        double newSalary;

        if (newName.isEmpty() || newEmail.isEmpty() || newPass.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Name, email, and password are required.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            newSalary = Double.parseDouble(salaryField.getText().trim());
        } catch (NumberFormatException nfe) {
            JOptionPane.showMessageDialog(this, "Invalid salary.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            boolean emailTaken = userService.checkEmailExists(newEmail);
            if (emailTaken && !newEmail.equalsIgnoreCase(employee.getEmail())) {
                JOptionPane.showMessageDialog(this, "Email already in use.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            employee.setFullName(newName);
            employee.setEmail(newEmail);
            employee.setPassword(newPass);
            employee.setJobTitle(newTitle);
            employee.setSalary(newSalary);

            boolean okUser = userService.updateUser(employee);
            boolean okEmp = employeeService.updateEmployeeDetails(employee);

            if (okUser || okEmp) {
                setStatus("Profile updated.");
                JOptionPane.showMessageDialog(this, "Profile updated.");
            } else {
                JOptionPane.showMessageDialog(this, "No changes or update failed.", "Info", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteProfile() {
        int sure = JOptionPane.showConfirmDialog(this,
                "This will permanently delete your employee record and user login.",
                "Warning", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
        if (sure != JOptionPane.OK_OPTION) return;

        String confirm = JOptionPane.showInputDialog(this, "Enter your email (" + employee.getEmail() + ") to confirm:");
        if (!employee.getEmail().equals(confirm)) {
            JOptionPane.showMessageDialog(this, "Email mismatch. Cancelled.");
            return;
        }

        employeeService.deleteEmployee(employee.getEmployeeId());
        boolean ok = userService.deleteUser(employee.getUserId());
        if (ok) {
            JOptionPane.showMessageDialog(this, "Profile deleted. Logging out.");
            dispose();
            loginFrame.backToLogin();
        } else {
            JOptionPane.showMessageDialog(this, "Deletion failed.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}