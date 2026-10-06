package in.edu.tint.it.salon.interfaces;

import in.edu.tint.it.salon.model.Booking;
import in.edu.tint.it.salon.model.Payment;
import in.edu.tint.it.salon.model.ServiceItem;
import java.time.LocalDate;
import java.util.List;

/**
 * Interface defining customer operations for Salon Management System.
 * Adheres to the interface separation principle specified in OOP lab architecture.
 */
public interface CustomerInt4Salon {
    List<ServiceItem> viewServices();
    Booking bookAppointment(int customerId, int staffId, int serviceId, LocalDate date, int hour, String paymentMethod) throws Exception;
    List<Booking> viewMyBookings(int customerId);
    boolean cancelBooking(int customerId, int bookingId);
    List<Payment> viewMyPayments(int customerId);
    boolean editProfile(int customerId, String newName, String newPhone);
    boolean changePassword(int customerId, String oldPassword, String newPassword);
}
