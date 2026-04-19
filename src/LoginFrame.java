import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.IOException;
import java.sql.SQLException;

/**
 * LoginFrame - includes email + password validation.
 */
public class LoginFrame extends JFrame {
    private final JTextField emailField = new JTextField();
    private final JPasswordField passwordField = new JPasswordField();
    private final JButton loginBtn = new JButton("Login");
    private final JButton registerBtn = new JButton("Register");

    private final AuthService authService = new AuthService();
    private final UserService userService = new UserService(new java.util.Scanner(System.in));

    private final JLabel statusLabel = new JLabel(" ");

    public LoginFrame() {
        super("Banking System - Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setSize(1000, 650);
        setMinimumSize(new Dimension(1000, 650));
        setLocationRelativeTo(null);

        Font base = UIManager.getFont("Label.font");
        Font labelFont = base.deriveFont(base.getSize() + 4f);
        Font fieldFont = UIManager.getFont("TextField.font").deriveFont(base.getSize() + 2f);
        Font btnFont = UIManager.getFont("Button.font").deriveFont(base.getSize() + 2f);
        Color headerBg = new Color(33, 150, 243);
        Color headerFg = Color.WHITE;

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(headerBg);
        header.setBorder(new EmptyBorder(18, 22, 18, 22));

        JLabel title = new JLabel("Welcome To Team_VoidMain Banking System", SwingConstants.CENTER);
        title.setForeground(headerFg);
        title.setFont(labelFont.deriveFont(Font.BOLD, labelFont.getSize() + 2f));

        JPanel centerTitlePanel = new JPanel(new GridBagLayout());
        centerTitlePanel.setOpaque(false);
        centerTitlePanel.add(title);
        header.add(centerTitlePanel, BorderLayout.CENTER);

        JPanel leftPlaceholder = new JPanel();
        leftPlaceholder.setOpaque(false);
        leftPlaceholder.setPreferredSize(new Dimension(120, 10));
        header.add(leftPlaceholder, BorderLayout.WEST);

        JPanel rightPlaceholder = new JPanel();
        rightPlaceholder.setOpaque(false);
        rightPlaceholder.setPreferredSize(new Dimension(120, 10));
        header.add(rightPlaceholder, BorderLayout.EAST);

        JPanel centerPanelForm = new JPanel(new GridBagLayout());
        centerPanelForm.setBackground(UIManager.getColor("Panel.background"));

        JPanel card = new JPanel(new BorderLayout());
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                new EmptyBorder(22, 28, 22, 28)
        ));
        card.setBackground(UIManager.getColor("Panel.background"));

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(UIManager.getColor("Panel.background"));
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(14, 14, 14, 14);
        gc.anchor = GridBagConstraints.WEST;
        gc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;

        JLabel emailLabel = new JLabel("Email:");
        emailLabel.setFont(labelFont);
        gc.gridx = 0; gc.gridy = row;
        form.add(emailLabel, gc);

        emailField.setColumns(35);
        emailField.setFont(fieldFont);
        gc.gridx = 1; gc.gridy = row++;
        form.add(emailField, gc);

        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setFont(labelFont);
        gc.gridx = 0; gc.gridy = row;
        form.add(passwordLabel, gc);

        passwordField.setColumns(35);
        passwordField.setFont(fieldFont);
        gc.gridx = 1; gc.gridy = row++;
        form.add(passwordField, gc);

        card.add(form, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 18, 12));
        buttons.setBackground(UIManager.getColor("Panel.background"));
        loginBtn.setFont(btnFont);
        registerBtn.setFont(btnFont);
        Dimension btnPref = new Dimension(140, 44);
        loginBtn.setPreferredSize(btnPref);
        registerBtn.setPreferredSize(btnPref);
        buttons.add(registerBtn);
        buttons.add(loginBtn);
        card.add(buttons, BorderLayout.SOUTH);

        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setBackground(UIManager.getColor("Panel.background"));
        GridBagConstraints wgc = new GridBagConstraints();
        wgc.insets = new Insets(6, 6, 6, 6);
        wgc.gridx = 0; wgc.gridy = 0;
        wrapper.add(card, wgc);

        centerPanelForm.add(wrapper);

        JPanel statusBar = new JPanel(new BorderLayout());
        statusBar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(220, 220, 220)));
        statusBar.setBackground(new Color(250, 250, 250));
        statusLabel.setFont(UIManager.getFont("Label.font").deriveFont(12f));
        statusLabel.setBorder(new EmptyBorder(6, 10, 6, 10));
        statusBar.add(statusLabel, BorderLayout.WEST);

        getContentPane().setLayout(new BorderLayout(8, 8));
        getContentPane().add(header, BorderLayout.NORTH);
        getContentPane().add(centerPanelForm, BorderLayout.CENTER);
        getContentPane().add(statusBar, BorderLayout.SOUTH);

        loginBtn.addActionListener(e -> doLogin());
        registerBtn.addActionListener(e -> openRegister());

        try {
            Image icon = ImageIO.read(getClass().getResourceAsStream("/icons/money_10894489.png"));
            if (icon != null) setIconImage(icon);
        } catch (Exception ignored) {}

        setVisible(true);
    }

    private void doLogin() {
        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword());

        // Required fields
        if (email.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Email and password are required.", "Error", JOptionPane.ERROR_MESSAGE);
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

        // Password validation
        if (password.length() < 6) {
            JOptionPane.showMessageDialog(this,
                    "Password must be at least 6 characters long.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            Person p = authService.login(email, password);
            if (p == null) {
                JOptionPane.showMessageDialog(this, "Invalid email or password.", "Login Failed", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (p instanceof Customer) {
                CustomerDashboardFrame frame = new CustomerDashboardFrame((Customer) p, this);
                frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
                frame.setVisible(true);
            } else if (p instanceof Employee) {
                EmployeeDashboardFrame frame = new EmployeeDashboardFrame((Employee) p, this);
                frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
                frame.setVisible(true);
            }

            setVisible(false);

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openRegister() {
        RegisterDialog dlg = new RegisterDialog(this);
        dlg.setVisible(true);

        Person p = dlg.getCreatedPerson();
        if (p != null) {
            if (p instanceof Customer) {
                CustomerDashboardFrame frame = new CustomerDashboardFrame((Customer) p, this);
                frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
                frame.setVisible(true);
            } else if (p instanceof Employee) {
                EmployeeDashboardFrame frame = new EmployeeDashboardFrame((Employee) p, this);
                frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
                frame.setVisible(true);
            }
            setVisible(false);
        }
    }

    void backToLogin() {
        passwordField.setText("");
        setVisible(true);
    }
}
