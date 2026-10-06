package in.edu.tint.it.salon.manager;

import in.edu.tint.it.salon.DataStore;
import in.edu.tint.it.salon.interfaces.CustomerInt4Salon;
import in.edu.tint.it.salon.model.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Vector;

/**
 * Business manager for Customer operations and profile lifecycle.
 * Implements CustomerInt4Salon and manages Vector<Customer> matching the instructor's architecture.
 */
public class CustomerManager implements CustomerInt4Salon {
    private final DataStore dataStore;
    private final Vector<Customer> customers;
    private final ServiceManager serviceManager;
    private final BookingManager bookingManager;
    private final PaymentManager paymentManager;

    public CustomerManager(DataStore dataStore, ServiceManager serviceManager,
                           BookingManager bookingManager, PaymentManager paymentManager) {
        this.dataStore = dataStore;
        this.customers = dataStore.customers;
        this.serviceManager = serviceManager;
        this.bookingManager = bookingManager;
        this.paymentManager = paymentManager;
    }

    public Customer authenticate(String username, String password) {
        for (Customer c : customers) {
            if (c.getUsername().equalsIgnoreCase(username) && c.checkPassword(password)) {
                c.setDelegateManager(this);
                return c;
            }
        }
        return null;
    }

    public Customer registerCustomer(String name, String phone, String username, String password) {
        if (dataStore.usernameTaken(username)) {
            throw new IllegalArgumentException("Username '" + username + "' is already taken.");
        }
        if (!phone.matches("\\d{10}")) {
            throw new IllegalArgumentException("Phone number must be exactly 10 digits.");
        }
        if (password == null || password.length() < 6 || password.contains("|")) {
            throw new IllegalArgumentException("Password must be at least 6 characters and cannot contain '|'.");
        }

        int id = dataStore.nextCustomerId();
        Customer customer = new Customer(id, name, phone, username, Person.hash(password));
        customer.setDelegateManager(this);
        customers.add(customer);
        dataStore.save();
        return customer;
    }

    public Customer getCustomerById(int id) {
        for (Customer c : customers) {
            if (c.getId() == id) {
                c.setDelegateManager(this);
                return c;
            }
        }
        return null;
    }

    public Vector<Customer> getAllCustomers() {
        return customers;
    }

    public boolean isUsernameTaken(String username) {
        return dataStore.usernameTaken(username);
    }

    // CustomerInt4Salon implementation
    @Override
    public List<ServiceItem> viewServices() {
        return serviceManager.getActiveServices();
    }

    @Override
    public Booking bookAppointment(int customerId, int staffId, int serviceId,
                                   LocalDate date, int hour, String paymentMethod) throws Exception {
        return bookingManager.createBooking(customerId, staffId, serviceId, date, hour, paymentMethod);
    }

    @Override
    public List<Booking> viewMyBookings(int customerId) {
        return bookingManager.getBookingsByCustomer(customerId);
    }

    @Override
    public boolean cancelBooking(int customerId, int bookingId) {
        return bookingManager.cancelBooking(customerId, bookingId);
    }

    @Override
    public List<Payment> viewMyPayments(int customerId) {
        return paymentManager.getPaymentsByCustomer(customerId);
    }

    @Override
    public boolean editProfile(int customerId, String newName, String newPhone) {
        Customer c = getCustomerById(customerId);
        if (c == null) return false;
        if (!newPhone.matches("\\d{10}")) {
            throw new IllegalArgumentException("Phone number must be exactly 10 digits.");
        }
        c.setName(newName);
        c.setPhone(newPhone);
        dataStore.save();
        return true;
    }

    @Override
    public boolean changePassword(int customerId, String oldPassword, String newPassword) {
        Customer c = getCustomerById(customerId);
        if (c == null || !c.checkPassword(oldPassword)) return false;
        if (newPassword == null || newPassword.length() < 6 || newPassword.contains("|")) {
            throw new IllegalArgumentException("New password must be at least 6 characters and cannot contain '|'.");
        }
        c.changePassword(newPassword);
        dataStore.save();
        return true;
    }
}
