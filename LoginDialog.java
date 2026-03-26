// ============================================================
//  LoginDialog.java  –  Simple Username/Password Login Screen
//  Uses Java Swing to build a modal dialog (pop-up window).
// ============================================================

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;

public class LoginDialog extends JDialog {

    // -------------------------------------------------------
    // These are the HARDCODED credentials for the mini-project.
    // In a real system you'd check a database. For college
    // viva purposes, simple hardcoded values are fine.
    // -------------------------------------------------------
    private static final String CORRECT_USERNAME = "admin";
    private static final String CORRECT_PASSWORD = "1234";

    // Flag that tells Main.java whether login succeeded
    private boolean loginSuccessful = false;

    // Swing input fields
    private JTextField usernameField;
    private JPasswordField passwordField;

    // -------------------------------------------------------
    // Constructor – builds the dialog window
    // 'parent' is the window that owns this dialog (null = no owner)
    // -------------------------------------------------------
    public LoginDialog(Frame parent) {
        super(parent, "🔐 Surveillance Login", true); // true = modal (blocks background)
        buildUI();
    }

    // Builds all Swing components inside the dialog
    private void buildUI() {

        // ---- Outer container panel ----
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(20, 30, 20, 30));
        mainPanel.setBackground(new Color(30, 30, 47)); // dark navy

        // ---- Title label at the top ----
        JLabel titleLabel = new JLabel("Smart Surveillance System", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(new Color(100, 200, 255));
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        // ---- Form panel (username + password) ----
        JPanel formPanel = new JPanel(new GridLayout(4, 1, 5, 5));
        formPanel.setBackground(new Color(30, 30, 47));

        // Username label
        JLabel userLabel = new JLabel("Username:");
        userLabel.setForeground(Color.WHITE);
        userLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        // Username text field
        usernameField = new JTextField();
        styleTextField(usernameField);

        // Password label
        JLabel passLabel = new JLabel("Password:");
        passLabel.setForeground(Color.WHITE);
        passLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        // Password field (shows dots instead of letters)
        passwordField = new JPasswordField();
        styleTextField(passwordField);

        formPanel.add(userLabel);
        formPanel.add(usernameField);
        formPanel.add(passLabel);
        formPanel.add(passwordField);
        mainPanel.add(formPanel, BorderLayout.CENTER);

        // ---- Login Button ----
        JButton loginBtn = new JButton("Login");
        loginBtn.setBackground(new Color(0, 150, 255));
        loginBtn.setForeground(Color.WHITE);
        loginBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        loginBtn.setFocusPainted(false);
        loginBtn.setBorderPainted(false);
        loginBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // What happens when user clicks Login
        loginBtn.addActionListener((ActionEvent e) -> attemptLogin());

        // Also allow pressing ENTER in the password field to login
        passwordField.addActionListener((ActionEvent e) -> attemptLogin());

        mainPanel.add(loginBtn, BorderLayout.SOUTH);

        // ---- Final dialog settings ----
        setContentPane(mainPanel);
        setSize(350, 260);
        setLocationRelativeTo(null); // center on screen
        setResizable(false);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }

    // Applies consistent dark styling to text fields
    private void styleTextField(JTextField field) {
        field.setBackground(new Color(50, 50, 70));
        field.setForeground(Color.WHITE);
        field.setCaretColor(Color.WHITE);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(100, 100, 150)),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)));
        field.setFont(new Font("Segoe UI", Font.PLAIN, 13));
    }

    // -------------------------------------------------------
    // Checks if username and password are correct.
    // Called when the Login button is clicked.
    // -------------------------------------------------------
    private void attemptLogin() {
        String enteredUser = usernameField.getText().trim();
        // getPassword() returns char[] for security (not String)
        String enteredPass = new String(passwordField.getPassword()).trim();

        if (enteredUser.equals(CORRECT_USERNAME) && enteredPass.equals(CORRECT_PASSWORD)) {
            loginSuccessful = true;
            dispose(); // close the dialog → returns control to Main.java
        } else {
            // Shake animation feedback – flash red border
            JOptionPane.showMessageDialog(this,
                    "❌ Incorrect username or password.\nHint: admin / 1234",
                    "Login Failed",
                    JOptionPane.ERROR_MESSAGE);
            passwordField.setText(""); // clear password field
            passwordField.requestFocus();
        }
    }

    // -------------------------------------------------------
    // Getter used by Main.java to check if login succeeded
    // -------------------------------------------------------
    public boolean isLoginSuccessful() {
        return loginSuccessful;
    }
}
