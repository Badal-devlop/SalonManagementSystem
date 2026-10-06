package in.edu.tint.it.salon.model;

import in.edu.tint.it.salon.interfaces.CustomerInt4Salon;
import java.time.LocalDate;
import java.util.List;

/**
 * POJO / JavaBean representing a salon Customer.
 * Implements CustomerInt4Salon for role-based contract support.
 */
public class Customer extends Person implements CustomerInt4Salon {

    private transient CustomerInt4Salon delegateManager;

    public Customer() {
        super();
    }

    public Customer(int id, String name, String phone, String username, String passwordHash) {
        super(id, name, phone, username, passwordHash);
    }

    public void setDelegateManager(CustomerInt4Salon manager) {
        this.delegateManager = manager;
    }

    public CustomerInt4Salon getDelegateManager() {
        return delegateManager;
    }

    @Override
    public String getRole() {
        return "Customer";
    }

    @Override
    public String toLine() {
        return baseLine();
    }

    public static Customer fromLine(String line) {
        String[] p = line.split("\\|", -1);
        return new Customer(Integer.parseInt(p[0]), p[1], p[2], p[3], p[4]);
    }

    // CustomerInt4Salon delegation
    @Override
    public List<ServiceItem> viewServices() {
        if (delegateManager != null) return delegateManager.viewServices();
        throw new UnsupportedOperationException("CustomerManager delegate required for service listing");
    }

    @Override
    public Booking bookAppointment(int customerId, int staffId, int serviceId, LocalDate date, int hour, String paymentMethod) throws Exception {
        if (delegateManager != null) return delegateManager.bookAppointment(customerId, staffId, serviceId, date, hour, paymentMethod);
        throw new UnsupportedOperationException("CustomerManager delegate required for booking appointment");
    }

    @Override
    public List<Booking> viewMyBookings(int customerId) {
        if (delegateManager != null) return delegateManager.viewMyBookings(customerId);
        throw new UnsupportedOperationException("CustomerManager delegate required for viewing bookings");
    }

    @Override
    public boolean cancelBooking(int customerId, int bookingId) {
        if (delegateManager != null) return delegateManager.cancelBooking(customerId, bookingId);
        throw new UnsupportedOperationException("CustomerManager delegate required for booking cancellation");
    }

    @Override
    public List<Payment> viewMyPayments(int customerId) {
        if (delegateManager != null) return delegateManager.viewMyPayments(customerId);
        throw new UnsupportedOperationException("CustomerManager delegate required for viewing payments");
    }

    @Override
    public boolean editProfile(int customerId, String newName, String newPhone) {
        if (delegateManager != null) return delegateManager.editProfile(customerId, newName, newPhone);
        setName(newName);
        setPhone(newPhone);
        return true;
    }

    @Override
    public boolean changePassword(int customerId, String oldPassword, String newPassword) {
        if (delegateManager != null) return delegateManager.changePassword(customerId, oldPassword, newPassword);
        if (checkPassword(oldPassword)) {
            changePassword(newPassword);
            return true;
        }
        return false;
    }
}
