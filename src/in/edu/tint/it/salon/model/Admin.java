package in.edu.tint.it.salon.model;

import in.edu.tint.it.salon.interfaces.AdminInt4Salon;
import java.util.List;
import java.util.Map;

/**
 * POJO / JavaBean representing an Admin entity.
 * Implements AdminInt4Salon for role-based contract support.
 */
public class Admin extends Person implements AdminInt4Salon {

    private transient AdminInt4Salon delegateManager;

    public Admin() {
        super();
    }

    public Admin(int id, String name, String phone, String username, String passwordHash) {
        super(id, name, phone, username, passwordHash);
    }

    public void setDelegateManager(AdminInt4Salon delegateManager) {
        this.delegateManager = delegateManager;
    }

    public AdminInt4Salon getDelegateManager() {
        return delegateManager;
    }

    @Override
    public String getRole() {
        return "Admin";
    }

    @Override
    public String toLine() {
        return baseLine();
    }

    public static Admin fromLine(String line) {
        String[] p = line.split("\\|", -1);
        return new Admin(Integer.parseInt(p[0]), p[1], p[2], p[3], p[4]);
    }

    // AdminInt4Salon delegation
    @Override
    public List<ServiceItem> viewAllServices() {
        if (delegateManager != null) return delegateManager.viewAllServices();
        throw new UnsupportedOperationException("AdminManager delegate required for service listing");
    }

    @Override
    public ServiceItem addService(String name, double price, int durationHours, String specialization) {
        if (delegateManager != null) return delegateManager.addService(name, price, durationHours, specialization);
        throw new UnsupportedOperationException("AdminManager delegate required for adding service");
    }

    @Override
    public boolean editService(int serviceId, double price, int durationHours, String specialization) {
        if (delegateManager != null) return delegateManager.editService(serviceId, price, durationHours, specialization);
        throw new UnsupportedOperationException("AdminManager delegate required for editing service");
    }

    @Override
    public boolean deactivateService(int serviceId) {
        if (delegateManager != null) return delegateManager.deactivateService(serviceId);
        throw new UnsupportedOperationException("AdminManager delegate required for deactivating service");
    }

    @Override
    public boolean reactivateService(int serviceId) {
        if (delegateManager != null) return delegateManager.reactivateService(serviceId);
        throw new UnsupportedOperationException("AdminManager delegate required for reactivating service");
    }

    @Override
    public List<Staff> viewAllStaff() {
        if (delegateManager != null) return delegateManager.viewAllStaff();
        throw new UnsupportedOperationException("AdminManager delegate required for staff listing");
    }

    @Override
    public Staff addStaff(String name, String phone, String username, String password, String specialization) {
        if (delegateManager != null) return delegateManager.addStaff(name, phone, username, password, specialization);
        throw new UnsupportedOperationException("AdminManager delegate required for adding staff");
    }

    @Override
    public boolean deactivateStaff(int staffId) {
        if (delegateManager != null) return delegateManager.deactivateStaff(staffId);
        throw new UnsupportedOperationException("AdminManager delegate required for deactivating staff");
    }

    @Override
    public boolean reactivateStaff(int staffId) {
        if (delegateManager != null) return delegateManager.reactivateStaff(staffId);
        throw new UnsupportedOperationException("AdminManager delegate required for reactivating staff");
    }

    @Override
    public List<Customer> viewAllCustomers() {
        if (delegateManager != null) return delegateManager.viewAllCustomers();
        throw new UnsupportedOperationException("AdminManager delegate required for customer listing");
    }

    @Override
    public List<Booking> viewAllBookings() {
        if (delegateManager != null) return delegateManager.viewAllBookings();
        throw new UnsupportedOperationException("AdminManager delegate required for bookings listing");
    }

    @Override
    public List<Payment> viewAllPayments() {
        if (delegateManager != null) return delegateManager.viewAllPayments();
        throw new UnsupportedOperationException("AdminManager delegate required for payments listing");
    }

    @Override
    public Map<String, Object> generateRevenueReport() {
        if (delegateManager != null) return delegateManager.generateRevenueReport();
        throw new UnsupportedOperationException("AdminManager delegate required for revenue report");
    }

    @Override
    public boolean changePassword(int adminId, String oldPassword, String newPassword) {
        if (delegateManager != null) return delegateManager.changePassword(adminId, oldPassword, newPassword);
        if (checkPassword(oldPassword)) {
            changePassword(newPassword);
            return true;
        }
        return false;
    }
}
