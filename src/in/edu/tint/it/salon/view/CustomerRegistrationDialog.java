package in.edu.tint.it.salon.view;

import in.edu.tint.it.salon.manager.CustomerManager;
import in.edu.tint.it.salon.model.Customer;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Registration dialog for new Salon customers.
 */
public class CustomerRegistrationDialog extends JDialog {
    private final CustomerManager customerManager;
    private final JTextField txtName;
    private final JTextField txtPhone;
    private final JTextField txtUsername;
    private final JPasswordField txtPassword;
    private final JPasswordField txtConfirmPassword;
    private boolean registered = false;
    private String registeredUsername = "";

    public CustomerRegistrationDialog(Frame parent, CustomerManager customerManager) {
        super(parent, "Customer Registration - Salon Management System", true);
        this.customerManager = customerManager;

        setSize(460, 480);
        setLocationRelativeTo(parent);
        setResizable(false);
        setLayout(new BorderLayout());

        // Header
        JPanel header = new JPanel(new GridLayout(2, 1, 0, 4));
        header.setBackground(UIUtils.COLOR_PRIMARY);
        header.setBorder(new EmptyBorder(16, 24, 16, 24));
        JLabel title = new JLabel("Create Customer Account");
        title.setFont(UIUtils.FONT_HEADER);
        title.setForeground(Color.WHITE);
        JLabel subtitle = new JLabel("Join our salon for instant bookings and special offers");
        subtitle.setFont(UIUtils.FONT_SMALL);
        subtitle.setForeground(new Color(203, 213, 225));
        header.add(title);
        header.add(subtitle);
        add(header, BorderLayout.NORTH);

        // Form Panel
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(UIUtils.COLOR_BG);
        form.setBorder(new EmptyBorder(20, 30, 20, 30));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 6, 6, 6);

        txtName = UIUtils.createTextField(20);
        txtPhone = UIUtils.createTextField(20);
        txtUsername = UIUtils.createTextField(20);
        txtPassword = UIUtils.createPasswordField(20);
        txtConfirmPassword = UIUtils.createPasswordField(20);

        int row = 0;
        addFormField(form, gbc, row++, "Full Name:", txtName);
        addFormField(form, gbc, row++, "Phone (10 Digits):", txtPhone);
        addFormField(form, gbc, row++, "Username:", txtUsername);
        addFormField(form, gbc, row++, "Password (min 6 chars):", txtPassword);
        addFormField(form, gbc, row++, "Confirm Password:", txtConfirmPassword);

        add(form, BorderLayout.CENTER);

        // Button Panel
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 16));
        btnPanel.setBackground(Color.WHITE);
        btnPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UIUtils.COLOR_BORDER));

        JButton btnCancel = UIUtils.createSecondaryButton("Cancel");
        JButton btnRegister = UIUtils.createAccentButton("Register Account");

        btnCancel.addActionListener(e -> dispose());
        btnRegister.addActionListener(e -> performRegistration());

        btnPanel.add(btnCancel);
        btnPanel.add(btnRegister);
        add(btnPanel, BorderLayout.SOUTH);
    }

    private void addFormField(JPanel panel, GridBagConstraints gbc, int row, String labelText, JComponent field) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.35;
        JLabel label = new JLabel(labelText);
        label.setFont(UIUtils.FONT_REGULAR_BOLD);
        label.setForeground(UIUtils.COLOR_TEXT_PRIMARY);
        panel.add(label, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.65;
        panel.add(field, gbc);
    }

    private void performRegistration() {
        String name = txtName.getText().trim();
        String phone = txtPhone.getText().trim();
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword()).trim();
        String confirm = new String(txtConfirmPassword.getPassword()).trim();

        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter your full name.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            txtName.requestFocus();
            return;
        }
        if (!phone.matches("\\d{10}")) {
            JOptionPane.showMessageDialog(this, "Phone number must be exactly 10 digits.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            txtPhone.requestFocus();
            return;
        }
        if (username.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please choose a username.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            txtUsername.requestFocus();
            return;
        }
        if (customerManager.isUsernameTaken(username)) {
            JOptionPane.showMessageDialog(this, "Username is already taken. Please choose another.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            txtUsername.requestFocus();
            return;
        }
        if (password.length() < 6 || password.contains("|")) {
            JOptionPane.showMessageDialog(this, "Password must be at least 6 characters and cannot contain '|'.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            txtPassword.requestFocus();
            return;
        }
        if (!password.equals(confirm)) {
            JOptionPane.showMessageDialog(this, "Passwords do not match.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            txtConfirmPassword.requestFocus();
            return;
        }

        try {
            Customer customer = customerManager.registerCustomer(name, phone, username, password);
            registered = true;
            registeredUsername = customer.getUsername();
            JOptionPane.showMessageDialog(this, "Registration successful! You can now log in.", "Success", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Registration failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isRegistered() {
        return registered;
    }

    public String getRegisteredUsername() {
        return registeredUsername;
    }
}
