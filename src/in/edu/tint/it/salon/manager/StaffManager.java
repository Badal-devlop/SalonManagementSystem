package in.edu.tint.it.salon.manager;

import in.edu.tint.it.salon.DataStore;
import in.edu.tint.it.salon.interfaces.StaffInt4Salon;
import in.edu.tint.it.salon.model.Booking;
import in.edu.tint.it.salon.model.Person;
import in.edu.tint.it.salon.model.ServiceItem;
import in.edu.tint.it.salon.model.Staff;
import java.util.List;
import java.util.Vector;
import java.util.stream.Collectors;

/**
 * Business manager for Staff operations.
 * Implements StaffInt4Salon and manages Vector<Staff> matching the instructor's architecture.
 */
public class StaffManager implements StaffInt4Salon {
    private final DataStore dataStore;
    private final Vector<Staff> staffList;
    private final BookingManager bookingManager;

    public StaffManager(DataStore dataStore, BookingManager bookingManager) {
        this.dataStore = dataStore;
        this.staffList = dataStore.staff;
        this.bookingManager = bookingManager;
    }

    public Staff authenticate(String username, String password) {
        for (Staff s : staffList) {
            if (s.getUsername().equalsIgnoreCase(username) && s.checkPassword(password)) {
                if (!s.isActive()) {
                    throw new IllegalStateException("This staff account has been deactivated.");
                }
                s.setDelegateManager(this);
                return s;
            }
        }
        return null;
    }

    public Vector<Staff> getAllStaff() {
        return staffList;
    }

    public List<Staff> getActiveStaff() {
        return staffList.stream().filter(Staff::isActive).collect(Collectors.toList());
    }

    public List<Staff> getInactiveStaff() {
        return staffList.stream().filter(s -> !s.isActive()).collect(Collectors.toList());
    }

    public List<Staff> getEligibleStaff(ServiceItem service) {
        return dataStore.eligibleStaff(service);
    }

    public Staff getStaffById(int id) {
        for (Staff s : staffList) {
            if (s.getId() == id) return s;
        }
        return null;
    }

    public Staff addStaff(String name, String phone, String username, String password, String specialization) {
        if (dataStore.usernameTaken(username)) {
            throw new IllegalArgumentException("Username '" + username + "' is already taken.");
        }
        int id = dataStore.nextStaffId();
        Staff staff = new Staff(id, name, phone, username, Person.hash(password), specialization, true);
        staffList.add(staff);
        dataStore.save();
        return staff;
    }

    public boolean deactivateStaff(int staffId) {
        Staff s = getStaffById(staffId);
        if (s == null || !s.isActive()) return false;

        boolean hasFuture = dataStore.bookings.stream().anyMatch(b ->
                b.getStaffId() == s.getId() &&
                b.getStatus() == Booking.Status.BOOKED &&
                bookingManager.isBookingInFuture(b));

        if (hasFuture) {
            throw new IllegalStateException(s.getName() + " still has upcoming booked appointments. Resolve them first.");
        }

        s.setActive(false);
        dataStore.save();
        return true;
    }

    public boolean reactivateStaff(int staffId) {
        Staff s = getStaffById(staffId);
        if (s == null || s.isActive()) return false;
        s.setActive(true);
        dataStore.save();
        return true;
    }

    // StaffInt4Salon implementation
    @Override
    public List<Booking> viewUpcomingAppointments(int staffId) {
        return bookingManager.getBookingsByStaff(staffId, Booking.Status.BOOKED);
    }

    @Override
    public boolean completeAppointment(int staffId, int bookingId) {
        return bookingManager.completeBooking(staffId, bookingId);
    }

    @Override
    public List<Booking> viewCompletedWork(int staffId) {
        return bookingManager.getBookingsByStaff(staffId, Booking.Status.COMPLETED);
    }

    @Override
    public boolean changePassword(int staffId, String oldPassword, String newPassword) {
        Staff s = getStaffById(staffId);
        if (s == null || !s.checkPassword(oldPassword)) return false;
        s.changePassword(newPassword);
        dataStore.save();
        return true;
    }
}
