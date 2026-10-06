package in.edu.tint.it.salon.view;

import in.edu.tint.it.salon.DataStore;
import in.edu.tint.it.salon.interfaces.StaffInt4Salon;
import in.edu.tint.it.salon.manager.BookingManager;
import in.edu.tint.it.salon.model.Booking;
import in.edu.tint.it.salon.model.Customer;
import in.edu.tint.it.salon.model.ServiceItem;
import in.edu.tint.it.salon.model.Staff;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Modern Swing Dashboard for Staff operations.
 */
public class StaffFrame extends JFrame {
    private final Staff staff;
    private final DataStore dataStore;
    private final StaffInt4Salon staffOps;
    private final BookingManager bookingManager;
    private final JFrame loginFrame;

    private JTable tblUpcoming;
    private DefaultTableModel modelUpcoming;
    private JTable tblCompleted;
    private DefaultTableModel modelCompleted;

    private JLabel lblCompletedCount;
    private JLabel lblTotalEarned;

    private JPasswordField txtCurrentPw;
    private JPasswordField txtNewPw;
    private JPasswordField txtConfirmPw;

    public StaffFrame(Staff staff, DataStore dataStore, StaffInt4Salon staffOps,
                      BookingManager bookingManager, JFrame loginFrame) {
        super("Staff Workspace - Salon Management System");
        this.staff = staff;
        this.dataStore = dataStore;
        this.staffOps = staffOps;
        this.bookingManager = bookingManager;
        this.loginFrame = loginFrame;

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(920, 640);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Header
        JPanel header = createHeader();
        add(header, BorderLayout.NORTH);

        // Tabbed Pane
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(UIUtils.FONT_REGULAR_BOLD);
        tabbedPane.setBackground(Color.WHITE);

        tabbedPane.addTab("Upcoming Appointments", createUpcomingPanel());
        tabbedPane.addTab("Completed Work & Earnings", createCompletedPanel());
        tabbedPane.addTab("Account Security", createSecurityPanel());

        tabbedPane.addChangeListener(e -> {
            int idx = tabbedPane.getSelectedIndex();
            if (idx == 0) loadUpcomingData();
            else if (idx == 1) loadCompletedData();
        });

        add(tabbedPane, BorderLayout.CENTER);

        loadUpcomingData();
        loadCompletedData();
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UIUtils.COLOR_PRIMARY);
        header.setBorder(new EmptyBorder(16, 24, 16, 24));

        JPanel left = new JPanel(new GridLayout(2, 1, 0, 4));
        left.setOpaque(false);
        JLabel title = new JLabel("Salon Staff Portal");
        title.setFont(UIUtils.FONT_TITLE);
        title.setForeground(Color.WHITE);

        JLabel sub = new JLabel(staff.getName() + " | Role: " + staff.getSpecialization() + " | ID: #" + staff.getId());
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

    private JPanel createUpcomingPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(UIUtils.COLOR_BG);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        String[] cols = {"Booking ID", "Date", "Scheduled Time", "Service", "Customer Name", "Customer Phone", "Price (Rs.)"};
        modelUpcoming = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        tblUpcoming = new JTable(modelUpcoming);
        UIUtils.styleTable(tblUpcoming);
        panel.add(new JScrollPane(tblUpcoming), BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        actions.setOpaque(false);

        JButton btnComplete = UIUtils.createAccentButton("Mark Appointment as Completed");
        btnComplete.addActionListener(e -> {
            int row = tblUpcoming.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Please select an appointment from the table to complete.", "Select Appointment", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int bookingId = (int) modelUpcoming.getValueAt(row, 0);
            try {
                boolean ok = staffOps.completeAppointment(staff.getId(), bookingId);
                if (ok) {
                    JOptionPane.showMessageDialog(this, "Appointment #" + bookingId + " marked as completed!", "Completed", JOptionPane.INFORMATION_MESSAGE);
                    loadUpcomingData();
                    loadCompletedData();
                } else {
                    JOptionPane.showMessageDialog(this, "Could not complete appointment.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (IllegalStateException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Time Validation Notice", JOptionPane.WARNING_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        JButton btnRefresh = UIUtils.createSecondaryButton("Refresh");
        btnRefresh.addActionListener(e -> loadUpcomingData());

        actions.add(btnRefresh);
        actions.add(btnComplete);
        panel.add(actions, BorderLayout.SOUTH);

        return panel;
    }

    private void loadUpcomingData() {
        modelUpcoming.setRowCount(0);
        List<Booking> list = staffOps.viewUpcomingAppointments(staff.getId());
        for (Booking b : list) {
            ServiceItem s = dataStore.serviceById(b.getServiceId());
            int duration = s == null ? 1 : s.getDurationHours();
            String time = String.format("%02d:00 - %02d:00", b.getHour(), b.getHour() + duration);
            Customer c = dataStore.customerById(b.getCustomerId());
            String phone = c == null ? "-" : c.getPhone();

            modelUpcoming.addRow(new Object[]{
                    b.getId(),
                    b.getDate().toString(),
                    time,
                    dataStore.serviceName(b.getServiceId()),
                    dataStore.customerName(b.getCustomerId()),
                    phone,
                    String.format("%.2f", b.getPrice())
            });
        }
    }

    private JPanel createCompletedPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(UIUtils.COLOR_BG);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Metric Card on top
        JPanel topCard = UIUtils.createCardPanel();
        topCard.setLayout(new GridLayout(1, 2, 20, 0));

        lblCompletedCount = new JLabel("Completed Appointments: 0");
        lblCompletedCount.setFont(UIUtils.FONT_HEADER);
        lblCompletedCount.setForeground(UIUtils.COLOR_PRIMARY);

        lblTotalEarned = new JLabel("Total Revenue Handled: Rs. 0.00");
        lblTotalEarned.setFont(UIUtils.FONT_HEADER);
        lblTotalEarned.setForeground(UIUtils.COLOR_ACCENT);

        topCard.add(lblCompletedCount);
        topCard.add(lblTotalEarned);
        panel.add(topCard, BorderLayout.NORTH);

        String[] cols = {"Booking ID", "Date", "Service", "Customer Name", "Amount (Rs.)", "Status"};
        modelCompleted = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        tblCompleted = new JTable(modelCompleted);
        UIUtils.styleTable(tblCompleted);
        panel.add(new JScrollPane(tblCompleted), BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        actions.setOpaque(false);
        JButton btnRefresh = UIUtils.createSecondaryButton("Refresh");
        btnRefresh.addActionListener(e -> loadCompletedData());
        actions.add(btnRefresh);
        panel.add(actions, BorderLayout.SOUTH);

        return panel;
    }

    private void loadCompletedData() {
        modelCompleted.setRowCount(0);
        List<Booking> list = staffOps.viewCompletedWork(staff.getId());
        double total = 0;
        for (Booking b : list) {
            total += b.getPrice();
            modelCompleted.addRow(new Object[]{
                    b.getId(),
                    b.getDate().toString(),
                    dataStore.serviceName(b.getServiceId()),
                    dataStore.customerName(b.getCustomerId()),
                    String.format("%.2f", b.getPrice()),
                    b.getStatus().toString()
            });
        }
        lblCompletedCount.setText("Completed Appointments: " + list.size());
        lblTotalEarned.setText("Total Revenue Handled: Rs. " + String.format("%.2f", total));
    }

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
        JLabel lblTitle = new JLabel("Change Account Password");
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

            boolean ok = staffOps.changePassword(staff.getId(), curr, nw);
            if (ok) {
                JOptionPane.showMessageDialog(this, "Password updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
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
