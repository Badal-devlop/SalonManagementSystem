package in.edu.tint.it.salon.view;

import in.edu.tint.it.salon.DataStore;
import in.edu.tint.it.salon.manager.*;
import in.edu.tint.it.salon.model.Admin;
import in.edu.tint.it.salon.model.Customer;
import in.edu.tint.it.salon.model.Staff;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * Main application window and Authentication portal.
 */
public class LoginFrame extends JFrame {
    private final DataStore dataStore;
    private final CustomerManager customerManager;
    private final StaffManager staffManager;
    private final AdminManager adminManager;
    private final ServiceManager serviceManager;
    private final BookingManager bookingManager;
    private final PaymentManager paymentManager;

    private JComboBox<String> cmbRole;
    private JTextField txtUsername;
    private JPasswordField txtPassword;

    public LoginFrame(DataStore dataStore, CustomerManager customerManager,
                      StaffManager staffManager, AdminManager adminManager,
                      ServiceManager serviceManager, BookingManager bookingManager,
                      PaymentManager paymentManager) {
        super("Salon Management System - Login");
        this.dataStore = dataStore;
        this.customerManager = customerManager;
        this.staffManager = staffManager;
        this.adminManager = adminManager;
        this.serviceManager = serviceManager;
        this.bookingManager = bookingManager;
        this.paymentManager = paymentManager;

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(780, 520);
        setLocationRelativeTo(null);
        setResizable(false);
        setLayout(new BorderLayout());

        // Header
        JPanel header = UIUtils.createHeaderBanner(
                "Techno Salon Management System",
                "Department of Information Technology - Desktop Application",
                null
        );
        add(header, BorderLayout.NORTH);

        // Center split: Info banner on left, Login card on right
        JPanel body = new JPanel(new GridLayout(1, 2, 20, 0));
        body.setBackground(UIUtils.COLOR_BG);
        body.setBorder(new EmptyBorder(25, 25, 25, 25));

        // Left Panel: Salon Info & Quick Demo credentials
        JPanel leftInfo = UIUtils.createCardPanel();
        leftInfo.setLayout(new BoxLayout(leftInfo, BoxLayout.Y_AXIS));

        JLabel lblWelcome = new JLabel("Welcome!");
        lblWelcome.setFont(UIUtils.FONT_HEADER);
        lblWelcome.setForeground(UIUtils.COLOR_PRIMARY);
        leftInfo.add(lblWelcome);
        leftInfo.add(Box.createVerticalStrut(8));

        JLabel lblDesc = new JLabel("<html>Manage appointments, styling services, staff allocations, and automated invoicing with MVC architecture.</html>");
        lblDesc.setFont(UIUtils.FONT_REGULAR);
        lblDesc.setForeground(UIUtils.COLOR_TEXT_MUTED);
        leftInfo.add(lblDesc);
        leftInfo.add(Box.createVerticalStrut(20));

        JLabel lblDemo = new JLabel("Default Demo Credentials:");
        lblDemo.setFont(UIUtils.FONT_SUBHEADER);
        lblDemo.setForeground(UIUtils.COLOR_PRIMARY);
        leftInfo.add(lblDemo);
        leftInfo.add(Box.createVerticalStrut(10));

        JPanel credBox = new JPanel(new GridLayout(4, 1, 0, 4));
        credBox.setOpaque(false);
        credBox.add(createCredLabel("Admin", "admin / admin123"));
        credBox.add(createCredLabel("Hair Stylist", "staff1 / staff123"));
        credBox.add(createCredLabel("Barber", "staff2 / staff123"));
        credBox.add(createCredLabel("Beautician", "staff3 / staff123"));
        leftInfo.add(credBox);
        leftInfo.add(Box.createVerticalGlue());

        body.add(leftInfo);

        // Right Panel: Login Form Card
        JPanel rightCard = UIUtils.createCardPanel();
        rightCard.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 6, 6, 6);

        JLabel lblSignIn = new JLabel("Sign In to Portal");
        lblSignIn.setFont(UIUtils.FONT_HEADER);
        lblSignIn.setForeground(UIUtils.COLOR_PRIMARY);

        cmbRole = new JComboBox<>(new String[]{"Customer", "Staff", "Admin"});
        cmbRole.setFont(UIUtils.FONT_REGULAR);
        cmbRole.setPreferredSize(new Dimension(cmbRole.getPreferredSize().width, 32));

        txtUsername = UIUtils.createTextField(15);
        txtPassword = UIUtils.createPasswordField(15);

        KeyAdapter enterListener = new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) performLogin();
            }
        };
        txtUsername.addKeyListener(enterListener);
        txtPassword.addKeyListener(enterListener);

        int r = 0;
        gbc.gridx = 0; gbc.gridy = r; gbc.gridwidth = 2;
        rightCard.add(lblSignIn, gbc);
        r++;
        gbc.gridwidth = 1;

        addLoginField(rightCard, gbc, r++, "Role / Portal:", cmbRole);
        addLoginField(rightCard, gbc, r++, "Username:", txtUsername);
        addLoginField(rightCard, gbc, r++, "Password:", txtPassword);

        JButton btnLogin = UIUtils.createPrimaryButton("Login");
        btnLogin.setPreferredSize(new Dimension(100, 36));
        btnLogin.addActionListener(e -> performLogin());

        gbc.gridx = 0; gbc.gridy = r; gbc.gridwidth = 2;
        gbc.insets = new Insets(12, 6, 6, 6);
        rightCard.add(btnLogin, gbc);
        r++;

        JButton btnRegister = UIUtils.createSecondaryButton("New Customer? Register Here");
        btnRegister.addActionListener(e -> {
            CustomerRegistrationDialog reg = new CustomerRegistrationDialog(this, customerManager);
            reg.setVisible(true);
            if (reg.isRegistered()) {
                cmbRole.setSelectedItem("Customer");
                txtUsername.setText(reg.getRegisteredUsername());
                txtPassword.requestFocus();
            }
        });

        gbc.gridx = 0; gbc.gridy = r; gbc.gridwidth = 2;
        gbc.insets = new Insets(6, 6, 6, 6);
        rightCard.add(btnRegister, gbc);

        body.add(rightCard);
        add(body, BorderLayout.CENTER);
    }

    private JLabel createCredLabel(String role, String creds) {
        JLabel l = new JLabel("• " + role + ": " + creds);
        l.setFont(UIUtils.FONT_SMALL);
        l.setForeground(UIUtils.COLOR_TEXT_PRIMARY);
        return l;
    }

    private void addLoginField(JPanel panel, GridBagConstraints gbc, int row, String label, JComponent comp) {
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.35;
        JLabel l = new JLabel(label);
        l.setFont(UIUtils.FONT_REGULAR_BOLD);
        l.setForeground(UIUtils.COLOR_TEXT_PRIMARY);
        panel.add(l, gbc);

        gbc.gridx = 1; gbc.weightx = 0.65;
        panel.add(comp, gbc);
    }

    private void performLogin() {
        String role = (String) cmbRole.getSelectedItem();
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword()).trim();

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter both username and password.", "Login Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if ("Customer".equalsIgnoreCase(role)) {
            Customer c = customerManager.authenticate(username, password);
            if (c != null) {
                txtPassword.setText("");
                setVisible(false);
                CustomerFrame cf = new CustomerFrame(c, dataStore, customerManager, serviceManager,
                        staffManager, bookingManager, paymentManager, this);
                cf.setVisible(true);
            } else {
                JOptionPane.showMessageDialog(this, "Invalid Customer username or password.", "Authentication Failed", JOptionPane.ERROR_MESSAGE);
            }
        } else if ("Staff".equalsIgnoreCase(role)) {
            try {
                Staff s = staffManager.authenticate(username, password);
                if (s != null) {
                    txtPassword.setText("");
                    setVisible(false);
                    StaffFrame sf = new StaffFrame(s, dataStore, staffManager, bookingManager, this);
                    sf.setVisible(true);
                } else {
                    JOptionPane.showMessageDialog(this, "Invalid Staff username or password.", "Authentication Failed", JOptionPane.ERROR_MESSAGE);
                }
            } catch (IllegalStateException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Account Inactive", JOptionPane.WARNING_MESSAGE);
            }
        } else if ("Admin".equalsIgnoreCase(role)) {
            Admin a = adminManager.authenticate(username, password);
            if (a != null) {
                txtPassword.setText("");
                setVisible(false);
                AdminFrame af = new AdminFrame(a, dataStore, adminManager, serviceManager, staffManager, bookingManager, this);
                af.setVisible(true);
            } else {
                JOptionPane.showMessageDialog(this, "Invalid Administrator username or password.", "Authentication Failed", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
