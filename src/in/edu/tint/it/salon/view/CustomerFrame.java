package in.edu.tint.it.salon.view;

import in.edu.tint.it.salon.DataStore;
import in.edu.tint.it.salon.InvoiceGenerator;
import in.edu.tint.it.salon.interfaces.CustomerInt4Salon;
import in.edu.tint.it.salon.manager.BookingManager;
import in.edu.tint.it.salon.manager.PaymentManager;
import in.edu.tint.it.salon.manager.ServiceManager;
import in.edu.tint.it.salon.manager.StaffManager;
import in.edu.tint.it.salon.model.Booking;
import in.edu.tint.it.salon.model.Customer;
import in.edu.tint.it.salon.model.Payment;
import in.edu.tint.it.salon.model.ServiceItem;
import in.edu.tint.it.salon.model.Staff;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * Modern Swing Dashboard for Customer operations.
 */
public class CustomerFrame extends JFrame {
    private final Customer customer;
    private final DataStore dataStore;
    private final CustomerInt4Salon customerOps;
    private final ServiceManager serviceManager;
    private final StaffManager staffManager;
    private final BookingManager bookingManager;
    private final PaymentManager paymentManager;
    private final JFrame loginFrame;

    // UI Components for Booking Tab
    private JComboBox<ServiceItemWrapper> cmbServices;
    private JComboBox<StaffWrapper> cmbStaff;
    private JTextField txtBookingDate;
    private JComboBox<String> cmbHours;
    private JComboBox<String> cmbPaymentMethod;
    private JLabel lblPriceSummary;
    private JLabel lblDurationSummary;

    // Tables
    private JTable tblServices;
    private DefaultTableModel modelServices;
    private JTable tblBookings;
    private DefaultTableModel modelBookings;
    private JTable tblPayments;
    private DefaultTableModel modelPayments;

    // Profile fields
    private JTextField txtProfileName;
    private JTextField txtProfilePhone;
    private JPasswordField txtCurrentPw;
    private JPasswordField txtNewPw;
    private JPasswordField txtConfirmPw;

    public CustomerFrame(Customer customer, DataStore dataStore, CustomerInt4Salon customerOps,
                         ServiceManager serviceManager, StaffManager staffManager,
                         BookingManager bookingManager, PaymentManager paymentManager,
                         JFrame loginFrame) {
        super("Customer Portal - Salon Management System");
        this.customer = customer;
        this.dataStore = dataStore;
        this.customerOps = customerOps;
        this.serviceManager = serviceManager;
        this.staffManager = staffManager;
        this.bookingManager = bookingManager;
        this.paymentManager = paymentManager;
        this.loginFrame = loginFrame;

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(950, 680);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Header
        JPanel header = createHeader();
        add(header, BorderLayout.NORTH);

        // Tabbed Pane
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(UIUtils.FONT_REGULAR_BOLD);
        tabbedPane.setBackground(Color.WHITE);

        tabbedPane.addTab("Available Services", createServicesPanel(tabbedPane));
        tabbedPane.addTab("Book Appointment", createBookingPanel());
        tabbedPane.addTab("My Bookings", createBookingsPanel());
        tabbedPane.addTab("Payment History", createPaymentsPanel());
        tabbedPane.addTab("My Profile", createProfilePanel());

        tabbedPane.addChangeListener(e -> {
            int idx = tabbedPane.getSelectedIndex();
            if (idx == 0) loadServicesData();
            else if (idx == 1) updateBookingFormServices();
            else if (idx == 2) loadBookingsData();
            else if (idx == 3) loadPaymentsData();
        });

        add(tabbedPane, BorderLayout.CENTER);

        loadServicesData();
        loadBookingsData();
        loadPaymentsData();
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UIUtils.COLOR_PRIMARY);
        header.setBorder(new EmptyBorder(16, 24, 16, 24));

        JPanel left = new JPanel(new GridLayout(2, 1, 0, 4));
        left.setOpaque(false);
        JLabel title = new JLabel("Salon Management System");
        title.setFont(UIUtils.FONT_TITLE);
        title.setForeground(Color.WHITE);
        JLabel sub = new JLabel("Welcome, " + customer.getName() + " | Phone: " + customer.getPhone());
        sub.setFont(UIUtils.FONT_REGULAR);
        sub.setForeground(new Color(203, 213, 225));
        left.add(title);
        left.add(sub);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        right.setOpaque(false);
        JButton btnLogout = UIUtils.createSecondaryButton("Logout");
        btnLogout.addActionListener(e -> {
            int opt = JOptionPane.showConfirmDialog(this, "Are you sure you want to log out?", "Logout", JOptionPane.YES_NO_OPTION);
            if (opt == JOptionPane.YES_OPTION) {
                dispose();
                if (loginFrame != null) loginFrame.setVisible(true);
            }
        });
        right.add(btnLogout);

        header.add(left, BorderLayout.WEST);
        header.add(right, BorderLayout.EAST);
        return header;
    }

    private JPanel createServicesPanel(JTabbedPane parentTabs) {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(UIUtils.COLOR_BG);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        String[] cols = {"ID", "Service Name", "Price (Rs.)", "Duration (Hours)", "Required Specialization"};
        modelServices = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        tblServices = new JTable(modelServices);
        UIUtils.styleTable(tblServices);
        JScrollPane scroll = new JScrollPane(tblServices);
        panel.add(scroll, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        actions.setOpaque(false);

        JButton btnBook = UIUtils.createAccentButton("Book Selected Service");
        btnBook.addActionListener(e -> {
            int row = tblServices.getSelectedRow();
            if (row >= 0) {
                int serviceId = (int) modelServices.getValueAt(row, 0);
                parentTabs.setSelectedIndex(1);
                selectServiceInBookingForm(serviceId);
            } else {
                JOptionPane.showMessageDialog(this, "Please select a service from the table to book.", "Select Service", JOptionPane.WARNING_MESSAGE);
            }
        });

        JButton btnRefresh = UIUtils.createSecondaryButton("Refresh");
        btnRefresh.addActionListener(e -> loadServicesData());

        actions.add(btnRefresh);
        actions.add(btnBook);
        panel.add(actions, BorderLayout.SOUTH);

        return panel;
    }

    private void loadServicesData() {
        modelServices.setRowCount(0);
        List<ServiceItem> list = customerOps.viewServices();
        for (ServiceItem s : list) {
            modelServices.addRow(new Object[]{
                    s.getId(),
                    s.getName(),
                    String.format("%.2f", s.getPrice()),
                    s.getDurationHours(),
                    s.getRequiredSpecialization()
            });
        }
    }

    private JPanel createBookingPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(UIUtils.COLOR_BG);
        panel.setBorder(new EmptyBorder(20, 30, 20, 30));

        JPanel card = UIUtils.createCardPanel();
        card.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 10, 8, 10);

        cmbServices = new JComboBox<>();
        cmbStaff = new JComboBox<>();
        txtBookingDate = UIUtils.createTextField(15);
        txtBookingDate.setText(LocalDate.now().plusDays(1).toString());
        cmbHours = new JComboBox<>();
        cmbPaymentMethod = new JComboBox<>(new String[]{"UPI", "Card", "Cash"});

        lblPriceSummary = new JLabel("Rs. 0.00");
        lblPriceSummary.setFont(UIUtils.FONT_HEADER);
        lblPriceSummary.setForeground(UIUtils.COLOR_ACCENT);

        lblDurationSummary = new JLabel("0 Hours");
        lblDurationSummary.setFont(UIUtils.FONT_REGULAR_BOLD);

        cmbServices.addActionListener(e -> updateStaffForSelectedService());
        cmbStaff.addActionListener(e -> refreshAvailableHours());

        JButton btnCheckSlots = UIUtils.createSecondaryButton("Check Available Slots");
        btnCheckSlots.addActionListener(e -> refreshAvailableHours());

        int row = 0;
        addFormRow(card, gbc, row++, "1. Select Service:", cmbServices);
        addFormRow(card, gbc, row++, "2. Select Eligible Staff:", cmbStaff);

        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.3;
        JLabel lblDate = new JLabel("3. Appointment Date (YYYY-MM-DD):");
        lblDate.setFont(UIUtils.FONT_REGULAR_BOLD);
        card.add(lblDate, gbc);

        gbc.gridx = 1; gbc.weightx = 0.7;
        JPanel datePanel = new JPanel(new BorderLayout(8, 0));
        datePanel.setOpaque(false);
        datePanel.add(txtBookingDate, BorderLayout.CENTER);
        datePanel.add(btnCheckSlots, BorderLayout.EAST);
        card.add(datePanel, gbc);
        row++;

        addFormRow(card, gbc, row++, "4. Available Start Time Slot:", cmbHours);
        addFormRow(card, gbc, row++, "5. Payment Method:", cmbPaymentMethod);
        addFormRow(card, gbc, row++, "Service Duration:", lblDurationSummary);
        addFormRow(card, gbc, row++, "Total Amount Payable:", lblPriceSummary);

        panel.add(card, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        btnPanel.setOpaque(false);
        JButton btnConfirm = UIUtils.createAccentButton("Confirm & Complete Booking");
        btnConfirm.setPreferredSize(new Dimension(240, 40));
        btnConfirm.addActionListener(e -> performBooking());
        btnPanel.add(btnConfirm);
        panel.add(btnPanel, BorderLayout.SOUTH);

        updateBookingFormServices();
        return panel;
    }

    private void addFormRow(JPanel panel, GridBagConstraints gbc, int row, String labelText, JComponent comp) {
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.3;
        JLabel lbl = new JLabel(labelText);
        lbl.setFont(UIUtils.FONT_REGULAR_BOLD);
        lbl.setForeground(UIUtils.COLOR_TEXT_PRIMARY);
        panel.add(lbl, gbc);

        gbc.gridx = 1; gbc.weightx = 0.7;
        panel.add(comp, gbc);
    }

    private static class ServiceItemWrapper {
        final ServiceItem item;
        ServiceItemWrapper(ServiceItem item) { this.item = item; }
        @Override public String toString() {
            return item.getName() + " - Rs. " + String.format("%.2f", item.getPrice()) +
                    " (" + item.getDurationHours() + " hr, " + item.getRequiredSpecialization() + ")";
        }
    }

    private static class StaffWrapper {
        final Staff staff;
        StaffWrapper(Staff staff) { this.staff = staff; }
        @Override public String toString() {
            return staff.getName() + " (" + staff.getSpecialization() + ")";
        }
    }

    private void updateBookingFormServices() {
        cmbServices.removeAllItems();
        List<ServiceItem> services = customerOps.viewServices();
        for (ServiceItem s : services) {
            cmbServices.addItem(new ServiceItemWrapper(s));
        }
        updateStaffForSelectedService();
    }

    private void selectServiceInBookingForm(int serviceId) {
        for (int i = 0; i < cmbServices.getItemCount(); i++) {
            ServiceItemWrapper w = cmbServices.getItemAt(i);
            if (w.item.getId() == serviceId) {
                cmbServices.setSelectedIndex(i);
                break;
            }
        }
    }

    private void updateStaffForSelectedService() {
        cmbStaff.removeAllItems();
        ServiceItemWrapper sel = (ServiceItemWrapper) cmbServices.getSelectedItem();
        if (sel == null) {
            lblPriceSummary.setText("Rs. 0.00");
            lblDurationSummary.setText("0 Hours");
            return;
        }

        lblPriceSummary.setText("Rs. " + String.format("%.2f", sel.item.getPrice()));
        lblDurationSummary.setText(sel.item.getDurationHours() + " Hour(s)");

        List<Staff> eligible = staffManager.getEligibleStaff(sel.item);
        for (Staff s : eligible) {
            cmbStaff.addItem(new StaffWrapper(s));
        }
        refreshAvailableHours();
    }

    private void refreshAvailableHours() {
        cmbHours.removeAllItems();
        ServiceItemWrapper serviceWrap = (ServiceItemWrapper) cmbServices.getSelectedItem();
        StaffWrapper staffWrap = (StaffWrapper) cmbStaff.getSelectedItem();
        if (serviceWrap == null || staffWrap == null) return;

        LocalDate date;
        try {
            date = LocalDate.parse(txtBookingDate.getText().trim());
        } catch (DateTimeParseException ex) {
            return;
        }

        List<Integer> free = bookingManager.getFreeHours(staffWrap.staff.getId(), date, serviceWrap.item.getDurationHours());
        if (free.isEmpty()) {
            cmbHours.addItem("No free slots on " + date);
        } else {
            for (int h : free) {
                int end = h + serviceWrap.item.getDurationHours();
                cmbHours.addItem(String.format("%02d:00 - %02d:00", h, end));
            }
        }
    }

    private void performBooking() {
        ServiceItemWrapper serviceWrap = (ServiceItemWrapper) cmbServices.getSelectedItem();
        StaffWrapper staffWrap = (StaffWrapper) cmbStaff.getSelectedItem();
        if (serviceWrap == null) {
            JOptionPane.showMessageDialog(this, "Please select a service.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (staffWrap == null) {
            JOptionPane.showMessageDialog(this, "No qualified staff available for this service.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        LocalDate date;
        try {
            date = LocalDate.parse(txtBookingDate.getText().trim());
            LocalDate today = LocalDate.now();
            if (date.isBefore(today)) {
                JOptionPane.showMessageDialog(this, "Booking date cannot be in the past.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (date.isAfter(today.plusDays(30))) {
                JOptionPane.showMessageDialog(this, "Bookings open only up to 30 days ahead.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this, "Invalid date format. Use YYYY-MM-DD (e.g. " + LocalDate.now().plusDays(1) + ")", "Date Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String selHourStr = (String) cmbHours.getSelectedItem();
        if (selHourStr == null || selHourStr.startsWith("No free")) {
            JOptionPane.showMessageDialog(this, "Please select a valid available time slot.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int startHour = Integer.parseInt(selHourStr.substring(0, 2));
        String method = (String) cmbPaymentMethod.getSelectedItem();

        int confirm = JOptionPane.showConfirmDialog(this,
                "Confirm appointment booking for " + serviceWrap.item.getName() + "\n" +
                "Staff: " + staffWrap.staff.getName() + "\n" +
                "Date: " + date + " at " + selHourStr + "\n" +
                "Amount: Rs. " + String.format("%.2f", serviceWrap.item.getPrice()) + " via " + method + "?",
                "Confirm Booking", JOptionPane.YES_NO_OPTION);

        if (confirm != JOptionPane.YES_OPTION) return;

        try {
            Booking b = customerOps.bookAppointment(customer.getId(), staffWrap.staff.getId(),
                    serviceWrap.item.getId(), date, startHour, method);

            loadBookingsData();
            loadPaymentsData();
            refreshAvailableHours();

            int opt = JOptionPane.showConfirmDialog(this,
                    "Booking successful! Your Booking ID is #" + b.getId() + ".\n" +
                    "Would you like to view/print your invoice now?",
                    "Booking Confirmed", JOptionPane.YES_NO_OPTION);

            if (opt == JOptionPane.YES_OPTION) {
                try {
                    String invoiceText = InvoiceGenerator.generate(dataStore, b);
                    new InvoiceDialog(this, invoiceText, b.getId()).setVisible(true);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Could not generate invoice: " + ex.getMessage(), "Invoice Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Booking failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JPanel createBookingsPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(UIUtils.COLOR_BG);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        String[] cols = {"Booking ID", "Date", "Time", "Service", "Staff", "Price (Rs.)", "Status"};
        modelBookings = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        tblBookings = new JTable(modelBookings);
        UIUtils.styleTable(tblBookings);
        panel.add(new JScrollPane(tblBookings), BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        actions.setOpaque(false);

        JButton btnInvoice = UIUtils.createSecondaryButton("View / Print Invoice");
        btnInvoice.addActionListener(e -> {
            int row = tblBookings.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Please select a booking to view its invoice.", "Select Booking", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int bookingId = (int) modelBookings.getValueAt(row, 0);
            Booking b = bookingManager.getBookingById(bookingId);
            if (b != null) {
                try {
                    String inv = InvoiceGenerator.generate(dataStore, b);
                    new InvoiceDialog(this, inv, b.getId()).setVisible(true);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Could not generate invoice: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        JButton btnCancel = UIUtils.createDangerButton("Cancel Selected Booking");
        btnCancel.addActionListener(e -> {
            int row = tblBookings.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Please select an active booking to cancel.", "Select Booking", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int bookingId = (int) modelBookings.getValueAt(row, 0);
            String status = (String) modelBookings.getValueAt(row, 6);
            if (!"BOOKED".equalsIgnoreCase(status)) {
                JOptionPane.showMessageDialog(this, "Only currently booked appointments can be cancelled.", "Cannot Cancel", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to cancel Booking #" + bookingId + "?\nPayment will be marked as refunded.",
                    "Confirm Cancellation", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    boolean ok = customerOps.cancelBooking(customer.getId(), bookingId);
                    if (ok) {
                        JOptionPane.showMessageDialog(this, "Booking cancelled successfully. Payment marked as refunded.", "Cancelled", JOptionPane.INFORMATION_MESSAGE);
                        loadBookingsData();
                        loadPaymentsData();
                    } else {
                        JOptionPane.showMessageDialog(this, "Could not cancel booking.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, ex.getMessage(), "Cancellation Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        JButton btnRefresh = UIUtils.createSecondaryButton("Refresh");
        btnRefresh.addActionListener(e -> loadBookingsData());

        actions.add(btnRefresh);
        actions.add(btnInvoice);
        actions.add(btnCancel);
        panel.add(actions, BorderLayout.SOUTH);

        return panel;
    }

    private void loadBookingsData() {
        modelBookings.setRowCount(0);
        List<Booking> bookings = customerOps.viewMyBookings(customer.getId());
        for (Booking b : bookings) {
            ServiceItem s = dataStore.serviceById(b.getServiceId());
            int duration = s == null ? 1 : s.getDurationHours();
            String time = String.format("%02d:00 - %02d:00", b.getHour(), b.getHour() + duration);
            modelBookings.addRow(new Object[]{
                    b.getId(),
                    b.getDate().toString(),
                    time,
                    dataStore.serviceName(b.getServiceId()),
                    dataStore.staffName(b.getStaffId()),
                    String.format("%.2f", b.getPrice()),
                    b.getStatus().toString()
            });
        }
    }

    private JPanel createPaymentsPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(UIUtils.COLOR_BG);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        String[] cols = {"Payment ID", "Booking ID", "Amount (Rs.)", "Payment Method", "Status"};
        modelPayments = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        tblPayments = new JTable(modelPayments);
        UIUtils.styleTable(tblPayments);
        panel.add(new JScrollPane(tblPayments), BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        actions.setOpaque(false);
        JButton btnRefresh = UIUtils.createSecondaryButton("Refresh");
        btnRefresh.addActionListener(e -> loadPaymentsData());
        actions.add(btnRefresh);
        panel.add(actions, BorderLayout.SOUTH);

        return panel;
    }

    private void loadPaymentsData() {
        modelPayments.setRowCount(0);
        List<Payment> payments = customerOps.viewMyPayments(customer.getId());
        for (Payment p : payments) {
            modelPayments.addRow(new Object[]{
                    p.getId(),
                    p.getBookingId(),
                    String.format("%.2f", p.getAmount()),
                    p.getMethod(),
                    p.getStatus().toString()
            });
        }
    }

    private JPanel createProfilePanel() {
        JPanel panel = new JPanel(new BorderLayout(20, 20));
        panel.setBackground(UIUtils.COLOR_BG);
        panel.setBorder(new EmptyBorder(25, 40, 25, 40));

        JPanel container = new JPanel(new GridLayout(1, 2, 25, 0));
        container.setOpaque(false);

        // Card 1: Personal Details
        JPanel cardInfo = UIUtils.createCardPanel();
        cardInfo.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 8, 8, 8);

        txtProfileName = UIUtils.createTextField(15);
        txtProfileName.setText(customer.getName());
        txtProfilePhone = UIUtils.createTextField(15);
        txtProfilePhone.setText(customer.getPhone());
        JTextField txtUsername = UIUtils.createTextField(15);
        txtUsername.setText(customer.getUsername());
        txtUsername.setEditable(false);

        int r = 0;
        gbc.gridx = 0; gbc.gridy = r; gbc.gridwidth = 2;
        JLabel lblProfileTitle = new JLabel("Personal Information");
        lblProfileTitle.setFont(UIUtils.FONT_HEADER);
        cardInfo.add(lblProfileTitle, gbc);
        r++;
        gbc.gridwidth = 1;

        addFormRow(cardInfo, gbc, r++, "Username (read-only):", txtUsername);
        addFormRow(cardInfo, gbc, r++, "Full Name:", txtProfileName);
        addFormRow(cardInfo, gbc, r++, "Phone Number:", txtProfilePhone);

        JButton btnSaveProfile = UIUtils.createPrimaryButton("Save Profile Changes");
        btnSaveProfile.addActionListener(e -> {
            String newName = txtProfileName.getText().trim();
            String newPhone = txtProfilePhone.getText().trim();
            if (newName.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Name cannot be empty.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (!newPhone.matches("\\d{10}")) {
                JOptionPane.showMessageDialog(this, "Phone must be 10 digits.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            try {
                customerOps.editProfile(customer.getId(), newName, newPhone);
                JOptionPane.showMessageDialog(this, "Profile updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        gbc.gridx = 1; gbc.gridy = r++;
        cardInfo.add(btnSaveProfile, gbc);
        container.add(cardInfo);

        // Card 2: Change Password
        JPanel cardPw = UIUtils.createCardPanel();
        cardPw.setLayout(new GridBagLayout());
        r = 0;
        gbc.gridx = 0; gbc.gridy = r; gbc.gridwidth = 2;
        JLabel lblPwTitle = new JLabel("Security & Password");
        lblPwTitle.setFont(UIUtils.FONT_HEADER);
        cardPw.add(lblPwTitle, gbc);
        r++;
        gbc.gridwidth = 1;

        txtCurrentPw = UIUtils.createPasswordField(15);
        txtNewPw = UIUtils.createPasswordField(15);
        txtConfirmPw = UIUtils.createPasswordField(15);

        addFormRow(cardPw, gbc, r++, "Current Password:", txtCurrentPw);
        addFormRow(cardPw, gbc, r++, "New Password:", txtNewPw);
        addFormRow(cardPw, gbc, r++, "Confirm Password:", txtConfirmPw);

        JButton btnChangePw = UIUtils.createAccentButton("Update Password");
        btnChangePw.addActionListener(e -> {
            String curr = new String(txtCurrentPw.getPassword()).trim();
            String nw = new String(txtNewPw.getPassword()).trim();
            String conf = new String(txtConfirmPw.getPassword()).trim();
            if (curr.isEmpty() || nw.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill in all password fields.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (!nw.equals(conf)) {
                JOptionPane.showMessageDialog(this, "New passwords do not match.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            try {
                boolean ok = customerOps.changePassword(customer.getId(), curr, nw);
                if (ok) {
                    JOptionPane.showMessageDialog(this, "Password changed successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    txtCurrentPw.setText("");
                    txtNewPw.setText("");
                    txtConfirmPw.setText("");
                } else {
                    JOptionPane.showMessageDialog(this, "Current password is incorrect.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        gbc.gridx = 1; gbc.gridy = r++;
        cardPw.add(btnChangePw, gbc);
        container.add(cardPw);

        panel.add(container, BorderLayout.NORTH);
        return panel;
    }
}
