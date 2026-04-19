// FULL FILE STARTS HERE

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

class CustomerDashboardFrame extends JFrame {
    private final Customer customer;
    private final LoginFrame loginFrame;

    private final AccountOperationsService accountOps = new AccountOperationsService();
    private final TransactionService transactionService = new TransactionService();
    private final UserService userService = new UserService(new java.util.Scanner(System.in));
    private final CustomerService customerService = new CustomerService();

    private final DefaultTableModel accountsModel = new DefaultTableModel(new Object[]{"Account No", "Type", "Balance"}, 0) {
        @Override public boolean isCellEditable(int row, int column) { return false; }
    };
    private final JTable accountsTable = new JTable(accountsModel);

    private final JTextField nameField = new JTextField();
    private final JTextField emailField = new JTextField();
    private final JPasswordField passwordField = new JPasswordField();
    private final JTextField addressField = new JTextField();
    private final JTextField phoneField = new JTextField();

    private final JLabel statusLabel = new JLabel(" ");

    private final NumberFormat currency = NumberFormat.getCurrencyInstance(Locale.US);

    CustomerDashboardFrame(Customer customer, LoginFrame loginFrame) {
        super("Banking System - Customer Dashboard");
        this.customer = customer;
        this.loginFrame = loginFrame;

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 650);
        setMinimumSize(new Dimension(1000, 650));
        setLocationRelativeTo(null);

        Font base = UIManager.getFont("Label.font");
        Font titleFont = base.deriveFont(base.getSize() + 4f).deriveFont(Font.BOLD);
        Font labelFont = base.deriveFont(base.getSize() + 3f);
        Font fieldFont = UIManager.getFont("TextField.font").deriveFont(base.getSize() + 2f);
        Font btnFont = UIManager.getFont("Button.font").deriveFont(base.getSize() + 2f);
        Color headerBg = new Color(33, 150, 243);
        Color headerFg = Color.WHITE;

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(headerBg);
        header.setBorder(new EmptyBorder(12, 18, 12, 18));
        JLabel title = new JLabel("Customer Dashboard - " + customer.getFullName());
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

        JPanel main = new JPanel(new BorderLayout(12, 12));
        main.setBorder(new EmptyBorder(14, 14, 14, 14));

        JPanel left = new JPanel(new BorderLayout(8, 8));
        left.setBorder(new TitledBorder("Accounts"));
        accountsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        accountsTable.setRowHeight(28);
        accountsTable.setFont(accountsTable.getFont().deriveFont(accountsTable.getFont().getSize() + 2f));
        JScrollPane accountsScroll = new JScrollPane(accountsTable);
        accountsScroll.setPreferredSize(new Dimension(520, 420));
        left.add(accountsScroll, BorderLayout.CENTER);

        JPanel actionRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 10));
        actionRow.setBorder(new EmptyBorder(6,6,6,6));

        JButton refreshBtn = new JButton("Refresh");
        JButton openBtn = new JButton("Open Account");
        JButton depositBtn = new JButton("Deposit");
        JButton withdrawBtn = new JButton("Withdraw");
        JButton transferBtn = new JButton("Transfer");
        JButton pinBtn = new JButton("Update PIN");
        JButton closeBtn = new JButton("Close Account");
        JButton historyBtn = new JButton("Transactions");

        JButton[] leftButtons = new JButton[]{refreshBtn, openBtn, depositBtn, withdrawBtn, transferBtn, pinBtn, closeBtn, historyBtn};

        // ⭐ FIXED BUTTON SIZE (Reduced to avoid overlap)
        Dimension leftBtnSize = new Dimension(110, 36);

        for (JButton b : leftButtons) {
            b.setFont(btnFont);
            b.setPreferredSize(leftBtnSize);
            actionRow.add(b);
        }
        left.add(actionRow, BorderLayout.NORTH);

        // Right profile pane
        JPanel right = new JPanel(new GridBagLayout());
        right.setBorder(new TitledBorder("Profile"));
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(12, 12, 12, 12);
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.anchor = GridBagConstraints.WEST;

        int row = 0;
        JLabel nameLbl = new JLabel("Full Name:"); nameLbl.setFont(labelFont);
        gc.gridx = 0; gc.gridy = row; right.add(nameLbl, gc);
        nameField.setFont(fieldFont); nameField.setColumns(30);
        gc.gridx = 1; right.add(nameField, gc); row++;

        JLabel emailLbl = new JLabel("Email:"); emailLbl.setFont(labelFont);
        gc.gridx = 0; gc.gridy = row; right.add(emailLbl, gc);
        emailField.setFont(fieldFont); emailField.setColumns(30);
        gc.gridx = 1; right.add(emailField, gc); row++;

        JLabel passLbl = new JLabel("Password:"); passLbl.setFont(labelFont);
        gc.gridx = 0; gc.gridy = row; right.add(passLbl, gc);
        passwordField.setFont(fieldFont); passwordField.setColumns(28);
        gc.gridx = 1; right.add(passwordField, gc); row++;

        JLabel addrLbl = new JLabel("Address:"); addrLbl.setFont(labelFont);
        gc.gridx = 0; gc.gridy = row; right.add(addrLbl, gc);
        addressField.setFont(fieldFont); addressField.setColumns(30);
        gc.gridx = 1; right.add(addressField, gc); row++;

        JLabel phoneLbl = new JLabel("Phone:"); phoneLbl.setFont(labelFont);
        gc.gridx = 0; gc.gridy = row; right.add(phoneLbl, gc);
        phoneField.setFont(fieldFont); phoneField.setColumns(20);
        gc.gridx = 1; right.add(phoneField, gc); row++;

        JPanel profileActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 8));
        JButton saveBtn = new JButton("Save");
        JButton deleteBtn = new JButton("Delete Profile");
        saveBtn.setFont(btnFont); saveBtn.setPreferredSize(new Dimension(140, 40));
        deleteBtn.setFont(btnFont); deleteBtn.setPreferredSize(new Dimension(160, 40));
        profileActions.add(deleteBtn);
        profileActions.add(saveBtn);

        gc.gridx = 0; gc.gridy = row; gc.gridwidth = 2;
        right.add(profileActions, gc);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, left, right);
        split.setResizeWeight(0.62);
        main.add(split, BorderLayout.CENTER);

        JPanel statusBar = new JPanel(new BorderLayout());
        statusBar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(220,220,220)));
        statusLabel.setFont(UIManager.getFont("Label.font").deriveFont(12f));
        statusLabel.setBorder(new EmptyBorder(6, 10, 6, 10));
        statusBar.add(statusLabel, BorderLayout.WEST);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(header, BorderLayout.NORTH);
        getContentPane().add(main, BorderLayout.CENTER);
        getContentPane().add(statusBar, BorderLayout.SOUTH);

        loadAccounts();
        loadProfile();

        refreshBtn.addActionListener(e -> loadAccounts());
        openBtn.addActionListener(e -> doOpenAccount());
        depositBtn.addActionListener(e -> doDeposit());
        withdrawBtn.addActionListener(e -> doWithdraw());
        transferBtn.addActionListener(e -> doTransfer());
        pinBtn.addActionListener(e -> doUpdatePin());
        closeBtn.addActionListener(e -> doCloseAccount());
        historyBtn.addActionListener(e -> showHistory());
        saveBtn.addActionListener(e -> saveProfile());
        deleteBtn.addActionListener(e -> deleteProfile());

        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setVisible(true);
    }

    private void setStatus(String msg) {
        statusLabel.setText(msg);
    }

    private void loadAccounts() {
        accountsModel.setRowCount(0);
        List<Account> accounts = accountOps.getAccountsForCustomer(customer.getCustomerId());
        for (Account a : accounts) {
            accountsModel.addRow(new Object[]{a.getAccountNumber(), a.getAccountType(), currency.format(a.getBalance())});
        }
        setStatus("Loaded " + accounts.size() + " account(s).");
    }

    private void loadProfile() {
        nameField.setText(customer.getFullName());
        emailField.setText(customer.getEmail());
        passwordField.setText(customer.getPassword());
        addressField.setText(customer.getAddress());
        phoneField.setText(customer.getPhoneNumber());
    }

    private Long getSelectedAccountNumber() {
        int row = accountsTable.getSelectedRow();
        if (row < 0) return null;
        Object val = accountsModel.getValueAt(row, 0);
        return (val instanceof Long) ? (Long) val : Long.parseLong(val.toString());
    }

    private Object showLargeOptionPane(Component parent, JComponent content, String title, int messageType) {
        JOptionPane pane = new JOptionPane(content, messageType, JOptionPane.OK_CANCEL_OPTION);
        JDialog dialog = pane.createDialog(SwingUtilities.getWindowAncestor(parent), title);
        dialog.setModal(true);
        dialog.setResizable(true);

        Dimension parentSize = (parent == null) ? Toolkit.getDefaultToolkit().getScreenSize() : parent.getSize();
        int w = Math.max(640, (int) (parentSize.width * 0.45));
        int h = Math.max(340, (int) (parentSize.height * 0.35));

        content.setPreferredSize(new Dimension(Math.min(w - 80, 780), Math.min(h - 80, 600)));

        dialog.pack();
        dialog.setSize(w, h);
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);
        return pane.getValue();
    }

    private void doOpenAccount() {
        String[] types = {"SAVINGS", "FIXED"};
        JComboBox<String> combo = new JComboBox<>(types);
        combo.setPreferredSize(new Dimension(240, 28));
        JPanel panel = new JPanel(new BorderLayout(8,8));
        panel.add(new JLabel("Select account type:"), BorderLayout.NORTH);
        panel.add(combo, BorderLayout.CENTER);

        Object result = showLargeOptionPane(this, panel, "Open Account", JOptionPane.QUESTION_MESSAGE);
        if (!(result instanceof Integer) || ((Integer) result) != JOptionPane.OK_OPTION) return;

        String type = (String) combo.getSelectedItem();
        try {
            Account acc = accountOps.openAccount(customer.getCustomerId(), type);
            if (acc == null) {
                JOptionPane.showMessageDialog(this, "Failed to open account.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            loadAccounts();
            setStatus("Opened account " + acc.getAccountNumber());
            JOptionPane.showMessageDialog(this,
                    "Account opened!\nAccount No: " + acc.getAccountNumber() + "\nPIN: " + acc.getSecurityPin(),
                    "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "DB error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void doDeposit() {
        Long accNo = getSelectedAccountNumber();
        if (accNo == null) {
            JOptionPane.showMessageDialog(this, "Select an account first.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(10,12,10,12);
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.gridx = 0; gc.gridy = 0;
        panel.add(new JLabel("Amount to deposit:"), gc);
        JTextField amtField = new JTextField();
        amtField.setPreferredSize(new Dimension(320, 30));
        gc.gridy = 1;
        panel.add(amtField, gc);

        Object res = showLargeOptionPane(this, panel, "Deposit", JOptionPane.QUESTION_MESSAGE);
        if (!(res instanceof Integer) || ((Integer) res) != JOptionPane.OK_OPTION) return;

        try {
            double amount = Double.parseDouble(amtField.getText().trim());
            Account updated = accountOps.deposit(accNo, amount);
            loadAccounts();
            setStatus("Deposit successful to " + accNo);
            JOptionPane.showMessageDialog(this, "Deposit successful. New balance: " + currency.format(updated.getBalance()));
        } catch (NumberFormatException nfe) {
            JOptionPane.showMessageDialog(this, "Invalid amount.", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void doWithdraw() {
        Long accNo = getSelectedAccountNumber();
        if (accNo == null) {
            JOptionPane.showMessageDialog(this, "Select an account first.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new EmptyBorder(8,12,8,12));
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(10,12,10,12);
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.gridx = 0; gc.gridy = 0;

        JLabel amtLabel = new JLabel("Amount to withdraw:");
        panel.add(amtLabel, gc);

        JTextField amtField = new JTextField();
        amtField.setPreferredSize(new Dimension(360, 32));
        gc.gridy = 1;
        panel.add(amtField, gc);

        JLabel pinLabel = new JLabel("PIN (4 digits):");
        gc.gridy = 2;
        panel.add(pinLabel, gc);

        JPasswordField pinField = new JPasswordField();
        pinField.setPreferredSize(new Dimension(200, 32));
        gc.gridy = 3;
        panel.add(pinField, gc);

        Object res = showLargeOptionPane(this, panel, "Withdraw", JOptionPane.QUESTION_MESSAGE);
        if (!(res instanceof Integer) || ((Integer) res) != JOptionPane.OK_OPTION) return;

        try {
            double amount = Double.parseDouble(amtField.getText().trim());
            String pin = new String(pinField.getPassword());
            Account updated = accountOps.withdraw(accNo, amount, pin);
            loadAccounts();
            setStatus("Withdrawal from " + accNo);
            JOptionPane.showMessageDialog(this, "Withdrawal successful. New balance: " + currency.format(updated.getBalance()));
        } catch (NumberFormatException nfe) {
            JOptionPane.showMessageDialog(this, "Invalid amount.", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (SecurityException se) {
            JOptionPane.showMessageDialog(this, se.getMessage(), "Security", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void doTransfer() {
        Long accNo = getSelectedAccountNumber();
        if (accNo == null) {
            JOptionPane.showMessageDialog(this, "Select a source account first.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JPanel outer = new JPanel(new BorderLayout());
        outer.setBorder(new EmptyBorder(12, 20, 12, 20));

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new EmptyBorder(8, 12, 8, 12));
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(12, 12, 12, 12);
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.anchor = GridBagConstraints.CENTER;

        Font labelFont = UIManager.getFont("Label.font").deriveFont(UIManager.getFont("Label.font").getSize() + 2f);
        Font fieldFont = UIManager.getFont("TextField.font").deriveFont(UIManager.getFont("TextField.font").getSize() + 2f);

        gc.gridx = 0; gc.gridy = 0; gc.weightx = 0.15;
        JLabel destLabel = new JLabel("Destination Account No:");
        destLabel.setFont(labelFont);
        destLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        panel.add(destLabel, gc);

        gc.gridx = 1; gc.gridy = 0; gc.weightx = 0.85;
        JTextField destField = new JTextField();
        destField.setFont(fieldFont);
        destField.setColumns(30);
        destField.setPreferredSize(new Dimension(520, 40));
        panel.add(destField, gc);

        gc.gridx = 0; gc.gridy = 1;
        JLabel amtLabel = new JLabel("Amount:");
        amtLabel.setFont(labelFont);
        amtLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        panel.add(amtLabel, gc);

        gc.gridx = 1; gc.gridy = 1;
        JTextField amtField = new JTextField();
        amtField.setFont(fieldFont);
        amtField.setColumns(20);
        amtField.setPreferredSize(new Dimension(360, 40));
        panel.add(amtField, gc);

        gc.gridx = 0; gc.gridy = 2;
        JLabel pinLabel = new JLabel("PIN (4 digits):");
        pinLabel.setFont(labelFont);
        pinLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        panel.add(pinLabel, gc);

        gc.gridx = 1; gc.gridy = 2;
        JPasswordField pinField = new JPasswordField();
        pinField.setFont(fieldFont);
        pinField.setColumns(10);
        pinField.setPreferredSize(new Dimension(260, 40));
        panel.add(pinField, gc);

        outer.add(panel, BorderLayout.CENTER);

        JLabel header = new JLabel("Enter transfer details");
        header.setFont(UIManager.getFont("Label.font").deriveFont(UIManager.getFont("Label.font").getSize() + 4f));
        header.setHorizontalAlignment(SwingConstants.CENTER);
        outer.add(header, BorderLayout.NORTH);

        outer.setPreferredSize(new Dimension(Math.max(720, getWidth() / 2), Math.max(360, getHeight() / 3)));

        Object res = showLargeOptionPane(this, outer, "Transfer", JOptionPane.QUESTION_MESSAGE);
        if (!(res instanceof Integer) || ((Integer) res) != JOptionPane.OK_OPTION) return;

        try {
            long dest = Long.parseLong(destField.getText().trim());
            double amount = Double.parseDouble(amtField.getText().trim());
            String pin = new String(pinField.getPassword());
            accountOps.transfer(accNo, dest, amount, pin);
            loadAccounts();
            setStatus("Transfer from " + accNo + " to " + dest);
            JOptionPane.showMessageDialog(this, "Transfer successful.");
        } catch (NumberFormatException nfe) {
            JOptionPane.showMessageDialog(this, "Invalid number/amount.", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (SecurityException se) {
            JOptionPane.showMessageDialog(this, se.getMessage(), "Security", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void doUpdatePin() {
        Long accNo = getSelectedAccountNumber();
        if (accNo == null) {
            JOptionPane.showMessageDialog(this, "Select an account first.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(8,8,8,8);
        gc.fill = GridBagConstraints.HORIZONTAL;

        gc.gridx = 0; gc.gridy = 0;
        panel.add(new JLabel("Enter new 4-digit PIN:"), gc);
        JPasswordField pinField = new JPasswordField();
        pinField.setPreferredSize(new Dimension(200, 28));
        gc.gridy = 1;
        panel.add(pinField, gc);

        Object res = showLargeOptionPane(this, panel, "Update PIN", JOptionPane.QUESTION_MESSAGE);
        if (!(res instanceof Integer) || ((Integer) res) != JOptionPane.OK_OPTION) return;

        try {
            String pin = new String(pinField.getPassword());
            if (!pin.matches("\\d{4}")) throw new IllegalArgumentException("PIN must be exactly 4 digits.");
            boolean ok = accountOps.updatePin(accNo, pin);
            if (ok) setStatus("PIN updated for " + accNo);
            JOptionPane.showMessageDialog(this, ok ? "PIN updated." : "PIN update failed.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void doCloseAccount() {
        Long accNo = getSelectedAccountNumber();
        if (accNo == null) {
            JOptionPane.showMessageDialog(this, "Select an account first.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JPanel panel = new JPanel(new BorderLayout(8,8));
        panel.add(new JLabel("<html><b>WARNING:</b> Closing this account will delete all its transaction history.</html>"),
                BorderLayout.NORTH);
        panel.add(new JLabel("Type DELETE in the box below to confirm:"), BorderLayout.CENTER);
        JTextField confirmField = new JTextField();
        confirmField.setPreferredSize(new Dimension(240, 28));
        panel.add(confirmField, BorderLayout.SOUTH);

        Object res = showLargeOptionPane(this, panel, "Confirm Account Closure", JOptionPane.WARNING_MESSAGE);
        if (!(res instanceof Integer) || ((Integer) res) != JOptionPane.OK_OPTION) return;

        if (!"DELETE".equals(confirmField.getText().trim())) {
            JOptionPane.showMessageDialog(this, "Account closure cancelled.");
            return;
        }

        try {
            boolean ok = accountOps.closeAccount(accNo);
            if (ok) {
                loadAccounts();
                setStatus("Closed account " + accNo);
                JOptionPane.showMessageDialog(this, "Account closed.");
            } else {
                JOptionPane.showMessageDialog(this, "Account closure failed.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (IllegalStateException ise) {
            JOptionPane.showMessageDialog(this, ise.getMessage(), "Cannot Close", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showHistory() {
        Long accNo = getSelectedAccountNumber();
        if (accNo == null) {
            JOptionPane.showMessageDialog(this, "Select an account first.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        java.util.List<Transaction> history = transactionService.getTransactionHistory(accNo);

        DefaultTableModel model = new DefaultTableModel(
                new Object[]{"Type", "Amount", "Description", "Time"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        for (Transaction t : history) {
            String sign = (t.getType().equals("DEPOSIT") || t.getType().equals("TRANSFER_IN")) ? "+" : "-";
            model.addRow(new Object[]{
                    t.getType(),
                    sign + currency.format(t.getAmount()),
                    "",
                    t.getTransactionTime().toString()
            });
        }

        JTable table = new JTable(model);
        table.setRowHeight(26);
        JScrollPane scroll = new JScrollPane(table);
        scroll.setPreferredSize(new Dimension(Math.max(820, getWidth()/2), Math.max(360, getHeight()/3)));

        JOptionPane pane = new JOptionPane(scroll, JOptionPane.PLAIN_MESSAGE, JOptionPane.DEFAULT_OPTION);
        JDialog dialog = pane.createDialog(this, "Transactions for " + accNo);
        dialog.setModal(true);
        dialog.setResizable(true);
        dialog.setSize(Math.max(900, getWidth()/2), Math.max(420, getHeight()/2));
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void saveProfile() {
        String newName = nameField.getText().trim();
        String newEmail = emailField.getText().trim();
        String newPass = new String(passwordField.getPassword());
        String newAddr = addressField.getText().trim();
        String newPhone = phoneField.getText().trim();

        if (newName.isEmpty() || newEmail.isEmpty() || newPass.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Name, email, and password are required.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            boolean emailTaken = userService.checkEmailExists(newEmail);
            if (emailTaken && !newEmail.equalsIgnoreCase(customer.getEmail())) {
                JOptionPane.showMessageDialog(this, "Email already in use.", "Error", JOptionPane.ERROR_MESSAGE);

                return;
            }

            customer.setFullName(newName);
            customer.setEmail(newEmail);
            customer.setPassword(newPass);
            customer.setAddress(newAddr);
            customer.setPhoneNumber(newPhone);

            boolean okUser = userService.updateUser(customer);
            boolean okCust = customerService.updateCustomerDetails(customer);

            if (okUser || okCust) {
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
                "This will permanently delete your profile, all accounts, and transactions.",
                "Warning", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
        if (sure != JOptionPane.OK_OPTION) return;

        String confirm = JOptionPane.showInputDialog(this,
                "Enter your email (" + customer.getEmail() + ") to confirm:");
        if (!customer.getEmail().equals(confirm)) {
            JOptionPane.showMessageDialog(this, "Email mismatch. Cancelled.");
            return;
        }

        customerService.deleteCustomer(customer.getCustomerId());
        boolean ok = userService.deleteUser(customer.getUserId());
        if (ok) {
            JOptionPane.showMessageDialog(this, "Your customer profile has been permanently deleted. You are now logged out.");
            dispose();
            loginFrame.backToLogin();
        } else {
            JOptionPane.showMessageDialog(this, "Deletion failed due to a database error.");
        }
    }
}

// FULL FILE ENDS HERE
