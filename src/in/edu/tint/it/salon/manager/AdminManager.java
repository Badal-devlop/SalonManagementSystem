package in.edu.tint.it.salon.manager;

import in.edu.tint.it.salon.DataStore;
import in.edu.tint.it.salon.interfaces.AdminInt4Salon;
import in.edu.tint.it.salon.model.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Business manager for Administration, service catalog, staff allocation, and reporting.
 * Implements AdminInt4Salon and manages Vector<Admin> matching the instructor's architecture.
 */
public class AdminManager implements AdminInt4Salon {
    private final DataStore dataStore;
    private final Vector<Admin> admins;
    private final ServiceManager serviceManager;
    private final StaffManager staffManager;
    private final BookingManager bookingManager;
    private final PaymentManager paymentManager;
    private final CustomerManager customerManager;

    public AdminManager(DataStore dataStore, ServiceManager serviceManager,
                        StaffManager staffManager, BookingManager bookingManager,
                        PaymentManager paymentManager, CustomerManager customerManager) {
        this.dataStore = dataStore;
        this.admins = dataStore.admins;
        this.serviceManager = serviceManager;
        this.staffManager = staffManager;
        this.bookingManager = bookingManager;
        this.paymentManager = paymentManager;
        this.customerManager = customerManager;
    }

    public Admin authenticate(String username, String password) {
        for (Admin a : admins) {
            if (a.getUsername().equalsIgnoreCase(username) && a.checkPassword(password)) {
                a.setDelegateManager(this);
                return a;
            }
        }
        return null;
    }

    public Vector<Admin> getAllAdmins() {
        return admins;
    }

    public Admin getAdminById(int id) {
        for (Admin a : admins) {
            if (a.getId() == id) {
                a.setDelegateManager(this);
                return a;
            }
        }
        return null;
    }

    // Service management delegation
    @Override
    public List<ServiceItem> viewAllServices() {
        return serviceManager.getAllServices();
    }

    @Override
    public ServiceItem addService(String name, double price, int durationHours, String specialization) {
        return serviceManager.addService(name, price, durationHours, specialization);
    }

    @Override
    public boolean editService(int serviceId, double price, int durationHours, String specialization) {
        return serviceManager.editService(serviceId, price, durationHours, specialization);
    }

    @Override
    public boolean deactivateService(int serviceId) {
        return serviceManager.deactivateService(serviceId);
    }

    @Override
    public boolean reactivateService(int serviceId) {
        return serviceManager.reactivateService(serviceId);
    }

    // Staff management delegation
    @Override
    public List<Staff> viewAllStaff() {
        return staffManager.getAllStaff();
    }

    @Override
    public Staff addStaff(String name, String phone, String username, String password, String specialization) {
        return staffManager.addStaff(name, phone, username, password, specialization);
    }

    @Override
    public boolean deactivateStaff(int staffId) {
        return staffManager.deactivateStaff(staffId);
    }

    @Override
    public boolean reactivateStaff(int staffId) {
        return staffManager.reactivateStaff(staffId);
    }

    // Customer & transaction viewing
    @Override
    public List<Customer> viewAllCustomers() {
        return customerManager.getAllCustomers();
    }

    @Override
    public List<Booking> viewAllBookings() {
        return bookingManager.getSortedBookings();
    }

    @Override
    public List<Payment> viewAllPayments() {
        return paymentManager.getAllPayments();
    }

    @Override
    public Map<String, Object> generateRevenueReport() {
        List<Booking> completed = dataStore.bookings.stream()
                .filter(b -> b.getStatus() == Booking.Status.COMPLETED)
                .collect(Collectors.toList());

        double totalRevenue = completed.stream().mapToDouble(Booking::getPrice).sum();

        Map<String, Object> report = new LinkedHashMap<>();
        report.put("completedCount", completed.size());
        report.put("totalRevenue", totalRevenue);

        List<Map<String, Object>> staffBreakdown = new ArrayList<>();
        for (Staff s : dataStore.staff) {
            long count = completed.stream().filter(b -> b.getStaffId() == s.getId()).count();
            double sum = completed.stream().filter(b -> b.getStaffId() == s.getId()).mapToDouble(Booking::getPrice).sum();
            Map<String, Object> sData = new HashMap<>();
            sData.put("staff", s);
            sData.put("count", count);
            sData.put("revenue", sum);
            staffBreakdown.add(sData);
        }
        report.put("staffBreakdown", staffBreakdown);

        return report;
    }

    @Override
    public boolean changePassword(int adminId, String oldPassword, String newPassword) {
        Admin a = getAdminById(adminId);
        if (a == null || !a.checkPassword(oldPassword)) return false;
        if (newPassword == null || newPassword.length() < 6 || newPassword.contains("|")) {
            throw new IllegalArgumentException("New password must be at least 6 characters and cannot contain '|'.");
        }
        a.changePassword(newPassword);
        dataStore.save();
        return true;
    }
}
