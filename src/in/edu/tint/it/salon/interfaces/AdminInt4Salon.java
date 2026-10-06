package in.edu.tint.it.salon.interfaces;

import in.edu.tint.it.salon.model.Admin;
import in.edu.tint.it.salon.model.Booking;
import in.edu.tint.it.salon.model.Customer;
import in.edu.tint.it.salon.model.Payment;
import in.edu.tint.it.salon.model.ServiceItem;
import in.edu.tint.it.salon.model.Staff;
import java.util.List;
import java.util.Map;

/**
 * Interface defining administrative operations for Salon Management System.
 */
public interface AdminInt4Salon {
    // Service Management
    List<ServiceItem> viewAllServices();
    ServiceItem addService(String name, double price, int durationHours, String specialization);
    boolean editService(int serviceId, double price, int durationHours, String specialization);
    boolean deactivateService(int serviceId);
    boolean reactivateService(int serviceId);

    // Staff Management
    List<Staff> viewAllStaff();
    Staff addStaff(String name, String phone, String username, String password, String specialization);
    boolean deactivateStaff(int staffId);
    boolean reactivateStaff(int staffId);

    // Customer & Transaction Views
    List<Customer> viewAllCustomers();
    List<Booking> viewAllBookings();
    List<Payment> viewAllPayments();

    // Reporting & Security
    Map<String, Object> generateRevenueReport();
    boolean changePassword(int adminId, String oldPassword, String newPassword);
}
