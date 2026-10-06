package in.edu.tint.it.salon.interfaces;

import in.edu.tint.it.salon.model.Booking;
import java.util.List;

/**
 * Interface defining staff operations for Salon Management System.
 */
public interface StaffInt4Salon {
    List<Booking> viewUpcomingAppointments(int staffId);
    boolean completeAppointment(int staffId, int bookingId);
    List<Booking> viewCompletedWork(int staffId);
    boolean changePassword(int staffId, String oldPassword, String newPassword);
}
