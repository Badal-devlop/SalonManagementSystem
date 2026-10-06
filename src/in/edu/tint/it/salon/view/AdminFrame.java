package in.edu.tint.it.salon.view;

import in.edu.tint.it.salon.DataStore;
import in.edu.tint.it.salon.InvoiceGenerator;
import in.edu.tint.it.salon.interfaces.AdminInt4Salon;
import in.edu.tint.it.salon.manager.BookingManager;
import in.edu.tint.it.salon.manager.ServiceManager;
import in.edu.tint.it.salon.manager.StaffManager;
import in.edu.tint.it.salon.model.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Modern Swing Dashboard for Administrator operations.
 */
public class AdminFrame extends JFrame {
    private final Admin admin;
    private final DataStore dataStore;
    private final AdminInt4Salon adminOps;
    private final ServiceManager serviceManager;
    private final StaffManager staffManager;
    private final BookingManager bookingManager;
    private final JFrame loginFrame;

    // Tables
    private JTable tblServices;
    private DefaultTableModel modelServices;

    private JTable tblStaff;
    private DefaultTableModel modelStaff;

    private JTable tblCustomers;
    private DefaultTableModel modelCustomers;

    private JTable tblBookings;
    private DefaultTableModel modelBookings;
    private JComboBox<String> cmbBookingFilter;

    private JTable tblPayments;
    private DefaultTableModel modelPayments;

    // Report
    private JLabel lblTotalRevenue;
    private JLabel lblTotalCompleted;
    private JLabel lblTotalCustomers;
    private JLabel lblTotalActiveStaff;
    private JTable tblStaffReport;
    private DefaultTableModel modelStaffReport;

    // Security
    private JPasswordField txtCurrentPw;
    private JPasswordField txtNewPw;
    private JPasswordField txtConfirmPw;

    public AdminFrame(Admin admin, DataStore dataStore, AdminInt4Salon adminOps,
                      ServiceManager serviceManager, StaffManager staffManager,
                      BookingManager bookingManager, JFrame loginFrame) {
        super("Administrator Console - Salon Management System");
        this.admin = admin;
        this.dataStore = dataStore;
        this.adminOps = adminOps;
        this.serviceManager = serviceManager;
        this.staffManager = staffManager;
        this.bookingManager = bookingManager;
        this.loginFrame = loginFrame;

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1040, 720);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Header
        JPanel header = createHeader();
        add(header, BorderLayout.NORTH);

        // Tabbed Pane
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(UIUtils.FONT_REGULAR_BOLD);
        tabbedPane.setBackground(Color.WHITE);

        tabbedPane.addTab("Services Management", createServicesPanel());
        tabbedPane.addTab("Staff Management", createStaffPanel());
        tabbedPane.addTab("Customer Accounts", createCustomersPanel());
        tabbedPane.addTab("All Bookings", createBookingsPanel());
        tabbedPane.addTab("Payment Records", createPaymentsPanel());
        tabbedPane.addTab("Revenue & Reports", createReportsPanel());
        tabbedPane.addTab("Account Security", createSecurityPanel());

        tabbedPane.addChangeListener(e -> {
            int idx = tabbedPane.getSelectedIndex();
            if (idx == 0) loadServicesData();
            else if (idx == 1) loadStaffData();
            else if (idx == 2) loadCustomersData();
            else if (idx == 3) loadBookingsData();
            else if (idx == 4) loadPaymentsData();
            else if (idx == 5) loadReportsData();
        });

        add(tabbedPane, BorderLayout.CENTER);

        loadServicesData();
        loadStaffData();
        loadCustomersData();
        loadBookingsData();
        loadPaymentsData();
        loadReportsData();
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UIUtils.COLOR_PRIMARY);
        header.setBorder(new EmptyBorder(16, 24, 16, 24));

        JPanel left = new JPanel(new GridLayout(2, 1, 0, 4));
        left.setOpaque(false);
        JLabel title = new JLabel("Salon Administration Dashboard");
        title.setFont(UIUtils.FONT_TITLE);
        title.setForeground(Color.WHITE);

        JLabel sub = new JLabel("Logged in as: " + admin.getName() + " (System Administrator)");
        sub.setFont(UIUtils.FONT_REGULAR);
        sub.setForeground(new Color(203, 213, 225));
        left.add(title);
        left.add(sub);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        right.setOpaque(false);
        JButton btnLogout = UIUtils.createSecondaryButton("Logout");
        btnLogout.addActionListener(e -> {
            int opt = JOptionPane.showConfirmDialog(this, "Are you sure you want to log out of Admin portal?", "Logout", JOptionPane.YES_NO_OPTION);
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

    // =========================================================================
    // Tab 1: Service Management
    // =========================================================================
    private JPanel createServicesPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(UIUtils.COLOR_BG);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        String[] cols = {"ID", "Service Name", "Price (Rs.)", "Duration (Hrs)", "Specialization", "Status"};
        modelServices = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        tblServices = new JTable(modelServices);
        UIUtils.styleTable(tblServices);
        panel.add(new JScrollPane(tblServices), BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setOpaque(false);

        JButton btnAdd = UIUtils.createAccentButton("Add Service");
        btnAdd.addActionListener(e -> showAddServiceDialog());

        JButton btnEdit = UIUtils.createSecondaryButton("Edit Service");
        btnEdit.addActionListener(e -> showEditServiceDialog());

        JButton btnDeactivate = UIUtils.createDangerButton("Deactivate");
        btnDeactivate.addActionListener(e -> {
            int row = tblServices.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Select a service to deactivate.", "Select Service", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int id = (int) modelServices.getValueAt(row, 0);
            String name = (String) modelServices.getValueAt(row, 1);
            if (JOptionPane.showConfirmDialog(this, "Deactivate service: " + name + "?", "Confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                adminOps.deactivateService(id);
                loadServicesData();
            }
        });

        JButton btnReactivate = UIUtils.createSecondaryButton("Reactivate");
        btnReactivate.addActionListener(e -> {
            int row = tblServices.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Select a service to reactivate.", "Select Service", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int id = (int) modelServices.getValueAt(row, 0);
            adminOps.reactivateService(id);
            loadServicesData();
        });

        JButton btnRefresh = UIUtils.createSecondaryButton("Refresh");
        btnRefresh.addActionListener(e -> loadServicesData());

        actions.add(btnRefresh);
        actions.add(btnAdd);
        actions.add(btnEdit);
        actions.add(btnDeactivate);
        actions.add(btnReactivate);
        panel.add(actions, BorderLayout.SOUTH);

        return panel;
    }

    private void loadServicesData() {
        modelServices.setRowCount(0);
        List<ServiceItem> list = adminOps.viewAllServices();
        for (ServiceItem s : list) {
            modelServices.addRow(new Object[]{
                    s.getId(),
                    s.getName(),
                    String.format("%.2f", s.getPrice()),
                    s.getDurationHours(),
                    s.getRequiredSpecialization(),
                    s.isActive() ? "Active" : "Inactive"
            });
        }
    }

    private void showAddServiceDialog() {
        JDialog dlg = new JDialog(this, "Add New Service", true);
        dlg.setSize(420, 360);
        dlg.setLocationRelativeTo(this);
        dlg.setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridLayout(4, 2, 10, 12));
        form.setBorder(new EmptyBorder(20, 24, 20, 24));
        form.setBackground(UIUtils.COLOR_BG);

        JTextField txtName = UIUtils.createTextField(15);
        JTextField txtPrice = UIUtils.createTextField(15);
        JComboBox<Integer> cmbDuration = new JComboBox<>(new Integer[]{1, 2, 3, 4});
        JTextField txtSpec = UIUtils.createTextField(15);

        form.add(new JLabel("Service Name:")); form.add(txtName);
        form.add(new JLabel("Price (Rs.):")); form.add(txtPrice);
        form.add(new JLabel("Duration (Hours):")); form.add(cmbDuration);
        form.add(new JLabel("Specialization:")); form.add(txtSpec);
        dlg.add(form, BorderLayout.CENTER);

        JPanel btnP = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        JButton btnSave = UIUtils.createAccentButton("Add Service");
        btnSave.addActionListener(e -> {
            String name = txtName.getText().trim();
            String spec = txtSpec.getText().trim();
            if (name.isEmpty() || spec.isEmpty()) {
                JOptionPane.showMessageDialog(dlg, "Please fill in all fields.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            double price;
            try {
                price = Double.parseDouble(txtPrice.getText().trim());
                if (price <= 0) throw new Exception();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dlg, "Enter a valid positive price.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            int dur = (Integer) cmbDuration.getSelectedItem();
            adminOps.addService(name, price, dur, spec);
            dlg.dispose();
            loadServicesData();
            JOptionPane.showMessageDialog(this, "Service added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
        });
        JButton btnCancel = UIUtils.createSecondaryButton("Cancel");
        btnCancel.addActionListener(e -> dlg.dispose());
        btnP.add(btnCancel);
        btnP.add(btnSave);
        dlg.add(btnP, BorderLayout.SOUTH);
        dlg.setVisible(true);
    }

    private void showEditServiceDialog() {
        int row = tblServices.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a service to edit.", "Select Service", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int id = (int) modelServices.getValueAt(row, 0);
        ServiceItem s = serviceManager.getServiceById(id);
        if (s == null) return;

        JDialog dlg = new JDialog(this, "Edit Service: " + s.getName(), true);
        dlg.setSize(420, 320);
        dlg.setLocationRelativeTo(this);
        dlg.setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridLayout(3, 2, 10, 12));
        form.setBorder(new EmptyBorder(20, 24, 20, 24));
        form.setBackground(UIUtils.COLOR_BG);

        JTextField txtPrice = UIUtils.createTextField(15);
        txtPrice.setText(String.valueOf(s.getPrice()));
        JComboBox<Integer> cmbDuration = new JComboBox<>(new Integer[]{1, 2, 3, 4});
        cmbDuration.setSelectedItem(s.getDurationHours());
        JTextField txtSpec = UIUtils.createTextField(15);
        txtSpec.setText(s.getRequiredSpecialization());

        form.add(new JLabel("Price (Rs.):")); form.add(txtPrice);
        form.add(new JLabel("Duration (Hours):")); form.add(cmbDuration);
        form.add(new JLabel("Specialization:")); form.add(txtSpec);
        dlg.add(form, BorderLayout.CENTER);

        JPanel btnP = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        JButton btnSave = UIUtils.createAccentButton("Update Service");
        btnSave.addActionListener(e -> {
            String spec = txtSpec.getText().trim();
            if (spec.isEmpty()) {
                JOptionPane.showMessageDialog(dlg, "Specialization cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            double price;
            try {
                price = Double.parseDouble(txtPrice.getText().trim());
                if (price <= 0) throw new Exception();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dlg, "Enter a valid positive price.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            int dur = (Integer) cmbDuration.getSelectedItem();
            adminOps.editService(id, price, dur, spec);
            dlg.dispose();
            loadServicesData();
            JOptionPane.showMessageDialog(this, "Service updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
        });
        JButton btnCancel = UIUtils.createSecondaryButton("Cancel");
        btnCancel.addActionListener(e -> dlg.dispose());
        btnP.add(btnCancel);
        btnP.add(btnSave);
        dlg.add(btnP, BorderLayout.SOUTH);
        dlg.setVisible(true);
    }

    // =========================================================================
    // Tab 2: Staff Management
    // =========================================================================
    private JPanel createStaffPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(UIUtils.COLOR_BG);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        String[] cols = {"ID", "Name", "Phone", "Specialization", "Username", "Status"};
        modelStaff = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        tblStaff = new JTable(modelStaff);
        UIUtils.styleTable(tblStaff);
        panel.add(new JScrollPane(tblStaff), BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setOpaque(false);

        JButton btnAdd = UIUtils.createAccentButton("Add Staff");
        btnAdd.addActionListener(e -> showAddStaffDialog());

        JButton btnDeactivate = UIUtils.createDangerButton("Deactivate");
        btnDeactivate.addActionListener(e -> {
            int row = tblStaff.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Select a staff member to deactivate.", "Select Staff", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int id = (int) modelStaff.getValueAt(row, 0);
            try {
                boolean ok = adminOps.deactivateStaff(id);
                if (ok) {
                    JOptionPane.showMessageDialog(this, "Staff member deactivated.", "Success", JOptionPane.INFORMATION_MESSAGE);
                    loadStaffData();
                } else {
                    JOptionPane.showMessageDialog(this, "Could not deactivate staff member.", "Notice", JOptionPane.WARNING_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Restriction", JOptionPane.WARNING_MESSAGE);
            }
        });

        JButton btnReactivate = UIUtils.createSecondaryButton("Reactivate");
        btnReactivate.addActionListener(e -> {
            int row = tblStaff.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Select a staff member to reactivate.", "Select Staff", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int id = (int) modelStaff.getValueAt(row, 0);
            adminOps.reactivateStaff(id);
            loadStaffData();
        });

        JButton btnRefresh = UIUtils.createSecondaryButton("Refresh");
        btnRefresh.addActionListener(e -> loadStaffData());

        actions.add(btnRefresh);
        actions.add(btnAdd);
        actions.add(btnDeactivate);
        actions.add(btnReactivate);
        panel.add(actions, BorderLayout.SOUTH);

        return panel;
    }

    private void loadStaffData() {
        modelStaff.setRowCount(0);
        List<Staff> list = adminOps.viewAllStaff();
        for (Staff s : list) {
            modelStaff.addRow(new Object[]{
                    s.getId(),
                    s.getName(),
                    s.getPhone(),
                    s.getSpecialization(),
                    s.getUsername(),
                    s.isActive() ? "Active" : "Inactive"
            });
        }
    }

    private void showAddStaffDialog() {
        JDialog dlg = new JDialog(this, "Add New Staff Member", true);
        dlg.setSize(440, 400);
        dlg.setLocationRelativeTo(this);
        dlg.setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridLayout(5, 2, 10, 12));
        form.setBorder(new EmptyBorder(20, 24, 20, 24));
        form.setBackground(UIUtils.COLOR_BG);

        JTextField txtName = UIUtils.createTextField(15);
        JTextField txtPhone = UIUtils.createTextField(15);
        JTextField txtSpec = UIUtils.createTextField(15);
        JTextField txtUser = UIUtils.createTextField(15);
        JPasswordField txtPw = UIUtils.createPasswordField(15);

        form.add(new JLabel("Full Name:")); form.add(txtName);
        form.add(new JLabel("Phone (10 Digits):")); form.add(txtPhone);
        form.add(new JLabel("Specialization:")); form.add(txtSpec);
        form.add(new JLabel("Username:")); form.add(txtUser);
        form.add(new JLabel("Temporary Password:")); form.add(txtPw);
        dlg.add(form, BorderLayout.CENTER);

        JPanel btnP = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        JButton btnSave = UIUtils.createAccentButton("Add Staff");
        btnSave.addActionListener(e -> {
            String name = txtName.getText().trim();
            String phone = txtPhone.getText().trim();
            String spec = txtSpec.getText().trim();
            String user = txtUser.getText().trim();
            String pw = new String(txtPw.getPassword()).trim();

            if (name.isEmpty() || spec.isEmpty() || user.isEmpty() || pw.isEmpty()) {
                JOptionPane.showMessageDialog(dlg, "Please fill in all fields.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (!phone.matches("\\d{10}")) {
                JOptionPane.showMessageDialog(dlg, "Phone number must be exactly 10 digits.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (pw.length() < 6 || pw.contains("|")) {
                JOptionPane.showMessageDialog(dlg, "Password must be at least 6 characters and cannot contain '|'.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            try {
                adminOps.addStaff(name, phone, user, pw, spec);
                dlg.dispose();
                loadStaffData();
                JOptionPane.showMessageDialog(this, "Staff member added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dlg, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        JButton btnCancel = UIUtils.createSecondaryButton("Cancel");
        btnCancel.addActionListener(e -> dlg.dispose());
        btnP.add(btnCancel);
        btnP.add(btnSave);
        dlg.add(btnP, BorderLayout.SOUTH);
        dlg.setVisible(true);
    }

    // =========================================================================
    // Tab 3: Customer Accounts
    // =========================================================================
    private JPanel createCustomersPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(UIUtils.COLOR_BG);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        String[] cols = {"Customer ID", "Full Name", "Phone Number", "Username"};
        modelCustomers = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        tblCustomers = new JTable(modelCustomers);
        UIUtils.styleTable(tblCustomers);
        panel.add(new JScrollPane(tblCustomers), BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setOpaque(false);
        JButton btnRefresh = UIUtils.createSecondaryButton("Refresh");
        btnRefresh.addActionListener(e -> loadCustomersData());
        actions.add(btnRefresh);
        panel.add(actions, BorderLayout.SOUTH);

        return panel;
    }

    private void loadCustomersData() {
        modelCustomers.setRowCount(0);
        List<Customer> list = adminOps.viewAllCustomers();
        for (Customer c : list) {
            modelCustomers.addRow(new Object[]{
                    c.getId(),
                    c.getName(),
                    c.getPhone(),
                    c.getUsername()
            });
        }
    }

    // =========================================================================
    // Tab 4: All Bookings
    // =========================================================================
    private JPanel createBookingsPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(UIUtils.COLOR_BG);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Filter Bar
        JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        filterBar.setOpaque(false);
        filterBar.add(new JLabel("Filter by Status:"));
        cmbBookingFilter = new JComboBox<>(new String[]{"All", "BOOKED", "COMPLETED", "CANCELLED"});
        cmbBookingFilter.addActionListener(e -> loadBookingsData());
        filterBar.add(cmbBookingFilter);
        panel.add(filterBar, BorderLayout.NORTH);

        String[] cols = {"Booking ID", "Date", "Time", "Service", "Customer", "Staff", "Status", "Price (Rs.)"};
        modelBookings = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        tblBookings = new JTable(modelBookings);
        UIUtils.styleTable(tblBookings);
        panel.add(new JScrollPane(tblBookings), BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setOpaque(false);

        JButton btnInvoice = UIUtils.createSecondaryButton("View / Print Invoice");
        btnInvoice.addActionListener(e -> {
            int row = tblBookings.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Select a booking to view invoice.", "Select Booking", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int bookingId = (int) modelBookings.getValueAt(row, 0);
            Booking b = bookingManager.getBookingById(bookingId);
            if (b != null) {
                try {
                    String inv = InvoiceGenerator.generate(dataStore, b);
                    new InvoiceDialog(this, inv, b.getId()).setVisible(true);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Could not generate invoice: " + ex.getMessage(), "Invoice Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        JButton btnRefresh = UIUtils.createSecondaryButton("Refresh");
        btnRefresh.addActionListener(e -> loadBookingsData());

        actions.add(btnRefresh);
        actions.add(btnInvoice);
        panel.add(actions, BorderLayout.SOUTH);

        return panel;
    }

    private void loadBookingsData() {
        modelBookings.setRowCount(0);
        String filter = cmbBookingFilter == null ? "All" : (String) cmbBookingFilter.getSelectedItem();
        List<Booking> list = adminOps.viewAllBookings();

        for (Booking b : list) {
            if (filter != null && !filter.equals("All") && !b.getStatus().toString().equalsIgnoreCase(filter)) {
                continue;
            }
            ServiceItem s = dataStore.serviceById(b.getServiceId());
            int duration = s == null ? 1 : s.getDurationHours();
            String time = String.format("%02d:00 - %02d:00", b.getHour(), b.getHour() + duration);

            modelBookings.addRow(new Object[]{
                    b.getId(),
                    b.getDate().toString(),
                    time,
                    dataStore.serviceName(b.getServiceId()),
                    dataStore.customerName(b.getCustomerId()),
                    dataStore.staffName(b.getStaffId()),
                    b.getStatus().toString(),
                    String.format("%.2f", b.getPrice())
            });
        }
    }

    // =========================================================================
    // Tab 5: Payment Records
    // =========================================================================
    private JPanel createPaymentsPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(UIUtils.COLOR_BG);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        String[] cols = {"Payment ID", "Booking ID", "Customer Name", "Amount (Rs.)", "Method", "Status"};
        modelPayments = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        tblPayments = new JTable(modelPayments);
        UIUtils.styleTable(tblPayments);
        panel.add(new JScrollPane(tblPayments), BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setOpaque(false);
        JButton btnRefresh = UIUtils.createSecondaryButton("Refresh");
        btnRefresh.addActionListener(e -> loadPaymentsData());
        actions.add(btnRefresh);
        panel.add(actions, BorderLayout.SOUTH);

        return panel;
    }

    private void loadPaymentsData() {
        modelPayments.setRowCount(0);
        List<Payment> list = adminOps.viewAllPayments();
        for (Payment p : list) {
            modelPayments.addRow(new Object[]{
                    p.getId(),
                    p.getBookingId(),
                    dataStore.customerName(p.getCustomerId()),
                    String.format("%.2f", p.getAmount()),
                    p.getMethod(),
                    p.getStatus().toString()
            });
        }
    }

    // =========================================================================
    // Tab 6: Revenue & Analytics
    // =========================================================================
    private JPanel createReportsPanel() {
        JPanel panel = new JPanel(new BorderLayout(20, 20));
        panel.setBackground(UIUtils.COLOR_BG);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Metric cards grid
        JPanel cardsGrid = new JPanel(new GridLayout(1, 4, 15, 0));
        cardsGrid.setOpaque(false);

        lblTotalRevenue = createMetricLabel("Total Revenue", "Rs. 0.00", UIUtils.COLOR_ACCENT);
        lblTotalCompleted = createMetricLabel("Completed Appointments", "0", UIUtils.COLOR_PRIMARY);
        lblTotalActiveStaff = createMetricLabel("Active Staff Members", "0", UIUtils.COLOR_SUCCESS);
        lblTotalCustomers = createMetricLabel("Registered Customers", "0", UIUtils.COLOR_PRIMARY_LIGHT);

        cardsGrid.add(createCardWrapper("Total Revenue", lblTotalRevenue));
        cardsGrid.add(createCardWrapper("Completed Appointments", lblTotalCompleted));
        cardsGrid.add(createCardWrapper("Active Staff", lblTotalActiveStaff));
        cardsGrid.add(createCardWrapper("Total Customers", lblTotalCustomers));
        panel.add(cardsGrid, BorderLayout.NORTH);

        // Staff Breakdown Table
        JPanel center = new JPanel(new BorderLayout(10, 10));
        center.setOpaque(false);
        JLabel lblTableTitle = new JLabel("Staff Performance & Revenue Breakdown");
        lblTableTitle.setFont(UIUtils.FONT_SUBHEADER);
        lblTableTitle.setForeground(UIUtils.COLOR_PRIMARY);
        center.add(lblTableTitle, BorderLayout.NORTH);

        String[] cols = {"Staff Name", "Specialization", "Completed Appointments", "Total Revenue Generated (Rs.)"};
        modelStaffReport = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        tblStaffReport = new JTable(modelStaffReport);
        UIUtils.styleTable(tblStaffReport);
        center.add(new JScrollPane(tblStaffReport), BorderLayout.CENTER);
        panel.add(center, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setOpaque(false);
        JButton btnRefresh = UIUtils.createSecondaryButton("Refresh Reports");
        btnRefresh.addActionListener(e -> loadReportsData());
        actions.add(btnRefresh);
        panel.add(actions, BorderLayout.SOUTH);

        return panel;
    }

    private JLabel createMetricLabel(String title, String initialVal, Color color) {
        JLabel l = new JLabel(initialVal, SwingConstants.CENTER);
        l.setFont(UIUtils.FONT_TITLE);
        l.setForeground(color);
        return l;
    }

    private JPanel createCardWrapper(String title, JLabel valueLabel) {
        JPanel card = UIUtils.createCardPanel();
        card.setLayout(new GridLayout(2, 1, 0, 4));
        JLabel t = new JLabel(title, SwingConstants.CENTER);
        t.setFont(UIUtils.FONT_SMALL);
        t.setForeground(UIUtils.COLOR_TEXT_MUTED);
        card.add(t);
        card.add(valueLabel);
        return card;
    }

    @SuppressWarnings("unchecked")
    private void loadReportsData() {
        Map<String, Object> rep = adminOps.generateRevenueReport();
        double rev = (Double) rep.get("totalRevenue");
        int count = (Integer) rep.get("completedCount");

        lblTotalRevenue.setText(String.format("Rs. %.2f", rev));
        lblTotalCompleted.setText(String.valueOf(count));
        lblTotalActiveStaff.setText(String.valueOf(staffManager.getActiveStaff().size()));
        lblTotalCustomers.setText(String.valueOf(dataStore.customers.size()));

        modelStaffReport.setRowCount(0);
        List<Map<String, Object>> breakdown = (List<Map<String, Object>>) rep.get("staffBreakdown");
        if (breakdown != null) {
            for (Map<String, Object> item : breakdown) {
                Staff s = (Staff) item.get("staff");
                long c = (Long) item.get("count");
                double r = (Double) item.get("revenue");
                modelStaffReport.addRow(new Object[]{
                        s.getName(),
                        s.getSpecialization(),
                        c,
                        String.format("%.2f", r)
                });
            }
        }
    }

    // =========================================================================
    // Tab 7: Account Security
    // =========================================================================
    private JPanel createSecurityPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(UIUtils.COLOR_BG);
        panel.setBorder(new EmptyBorder(30, 40, 30, 40));

        JPanel card = UIUtils.createCardPanel();
        card.setLayout(new GridBagLayout());
        card.setMaximumSize(new Dimension(500, 300));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 10, 10, 10);

        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        JLabel lblTitle = new JLabel("Change Administrator Password");
        lblTitle.setFont(UIUtils.FONT_HEADER);
        card.add(lblTitle, gbc);
        gbc.gridwidth = 1;

        txtCurrentPw = UIUtils.createPasswordField(15);
        txtNewPw = UIUtils.createPasswordField(15);
        txtConfirmPw = UIUtils.createPasswordField(15);

        int r = 1;
        addSecurityRow(card, gbc, r++, "Current Password:", txtCurrentPw);
        addSecurityRow(card, gbc, r++, "New Password:", txtNewPw);
        addSecurityRow(card, gbc, r++, "Confirm New Password:", txtConfirmPw);

        JButton btnChange = UIUtils.createAccentButton("Update Password");
        btnChange.addActionListener(e -> {
            String curr = new String(txtCurrentPw.getPassword()).trim();
            String nw = new String(txtNewPw.getPassword()).trim();
            String conf = new String(txtConfirmPw.getPassword()).trim();

            if (curr.isEmpty() || nw.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter all password fields.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (nw.length() < 6 || nw.contains("|")) {
                JOptionPane.showMessageDialog(this, "Password must be at least 6 characters and cannot contain '|'.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (!nw.equals(conf)) {
                JOptionPane.showMessageDialog(this, "New passwords do not match.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            boolean ok = adminOps.changePassword(admin.getId(), curr, nw);
            if (ok) {
                JOptionPane.showMessageDialog(this, "Administrator password updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                txtCurrentPw.setText("");
                txtNewPw.setText("");
                txtConfirmPw.setText("");
            } else {
                JOptionPane.showMessageDialog(this, "Current password is incorrect.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        gbc.gridx = 1; gbc.gridy = r;
        card.add(btnChange, gbc);

        JPanel wrapper = new JPanel(new FlowLayout(FlowLayout.CENTER));
        wrapper.setOpaque(false);
        wrapper.add(card);
        panel.add(wrapper, BorderLayout.NORTH);
        return panel;
    }

    private void addSecurityRow(JPanel panel, GridBagConstraints gbc, int row, String label, JComponent field) {
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.35;
        JLabel l = new JLabel(label);
        l.setFont(UIUtils.FONT_REGULAR_BOLD);
        panel.add(l, gbc);

        gbc.gridx = 1; gbc.weightx = 0.65;
        panel.add(field, gbc);
    }
}
