package in.edu.tint.it.salon.manager;

import in.edu.tint.it.salon.DataStore;
import in.edu.tint.it.salon.model.Booking;
import in.edu.tint.it.salon.model.Payment;
import in.edu.tint.it.salon.model.ServiceItem;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Vector;
import java.util.stream.Collectors;

/**
 * Business manager for Appointment Bookings, slot validations, cancellations and completion.
 * Manages Vector<Booking> adhering to the lab architectural specifications.
 */
public class BookingManager {
    private final DataStore dataStore;
    private final Vector<Booking> bookings;
    private final PaymentManager paymentManager;

    public BookingManager(DataStore dataStore, PaymentManager paymentManager) {
        this.dataStore = dataStore;
        this.bookings = dataStore.bookings;
        this.paymentManager = paymentManager;
    }

    public Vector<Booking> getAllBookings() {
        return bookings;
    }

    public List<Booking> getSortedBookings() {
        return bookings.stream().sorted(byDateTime()).collect(Collectors.toList());
    }

    public List<Booking> getBookingsByCustomer(int customerId) {
        return bookings.stream()
                .filter(b -> b.getCustomerId() == customerId)
                .sorted(byDateTime())
                .collect(Collectors.toList());
    }

    public List<Booking> getBookingsByStaff(int staffId, Booking.Status status) {
        return bookings.stream()
                .filter(b -> b.getStaffId() == staffId && (status == null || b.getStatus() == status))
                .sorted(byDateTime())
                .collect(Collectors.toList());
    }

    public Booking getBookingById(int id) {
        for (Booking b : bookings) {
            if (b.getId() == id) return b;
        }
        return null;
    }

    public boolean isBookingInFuture(Booking b) {
        ServiceItem service = dataStore.serviceById(b.getServiceId());
        int duration = service == null ? 1 : service.getDurationHours();
        LocalDateTime end = b.getDate().atTime(b.getHour(), 0).plusHours(duration);
        return end.isAfter(LocalDateTime.now());
    }

    public boolean isAppointmentFinished(Booking b) {
        ServiceItem service = dataStore.serviceById(b.getServiceId());
        int duration = service == null ? 1 : service.getDurationHours();
        LocalDateTime end = b.getDate().atTime(b.getHour(), 0).plusHours(duration);
        return !end.isAfter(LocalDateTime.now());
    }

    public boolean isSlotTaken(int staffId, LocalDate date, int startHour, int durationHours) {
        return dataStore.slotTaken(staffId, date, startHour, durationHours);
    }

    public List<Integer> getFreeHours(int staffId, LocalDate date, int durationHours) {
        return dataStore.freeHours(staffId, date, durationHours);
    }

    public Booking createBooking(int customerId, int staffId, int serviceId,
                                 LocalDate date, int hour, String paymentMethod) throws Exception {
        ServiceItem service = dataStore.serviceById(serviceId);
        if (service == null || !service.isActive()) {
            throw new IllegalArgumentException("Service is not available.");
        }

        if (isSlotTaken(staffId, date, hour, service.getDurationHours())) {
            throw new IllegalStateException("Selected slot has just been taken. Please choose another time.");
        }

        int bookingId = dataStore.nextBookingId();
        Booking booking = new Booking(bookingId, customerId, staffId, serviceId,
                date, hour, Booking.Status.BOOKED, service.getPrice());
        bookings.add(booking);

        paymentManager.recordPayment(bookingId, customerId, booking.getPrice(), paymentMethod);
        dataStore.save();
        return booking;
    }

    public boolean cancelBooking(int customerId, int bookingId) {
        Booking b = getBookingById(bookingId);
        if (b == null || b.getCustomerId() != customerId || b.getStatus() != Booking.Status.BOOKED) {
            return false;
        }
        if (!isBookingInFuture(b)) {
            throw new IllegalStateException("An appointment that has already started cannot be cancelled.");
        }
        b.setStatus(Booking.Status.CANCELLED);
        paymentManager.refundPayment(b.getId());
        dataStore.save();
        return true;
    }

    public boolean completeBooking(int staffId, int bookingId) {
        Booking b = getBookingById(bookingId);
        if (b == null || b.getStaffId() != staffId || b.getStatus() != Booking.Status.BOOKED) {
            return false;
        }
        if (!isAppointmentFinished(b)) {
            throw new IllegalStateException("The appointment end time must pass before it can be completed.");
        }
        b.setStatus(Booking.Status.COMPLETED);
        dataStore.save();
        return true;
    }

    public static Comparator<Booking> byDateTime() {
        return Comparator.comparing(Booking::getDate).thenComparingInt(Booking::getHour);
    }
}
