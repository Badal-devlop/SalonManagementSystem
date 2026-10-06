package in.edu.tint.it.salon.model;

import in.edu.tint.it.salon.interfaces.StaffInt4Salon;
import java.util.List;

/**
 * POJO / JavaBean representing a salon Staff member.
 * Implements StaffInt4Salon for role-based contract support.
 */
public class Staff extends Person implements StaffInt4Salon {
    private String specialization;
    private boolean active;
    private transient StaffInt4Salon delegateManager;

    public Staff() {
        super();
    }

    public Staff(int id, String name, String phone, String username, String passwordHash,
                 String specialization, boolean active) {
        super(id, name, phone, username, passwordHash);
        this.specialization = specialization;
        this.active = active;
    }

    public void setDelegateManager(StaffInt4Salon delegateManager) {
        this.delegateManager = delegateManager;
    }

    public StaffInt4Salon getDelegateManager() {
        return delegateManager;
    }

    @Override
    public String getRole() {
        return "Staff";
    }

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public String toLine() {
        return baseLine() + "|" + specialization + "|" + active;
    }

    public static Staff fromLine(String line) {
        String[] p = line.split("\\|", -1);
        return new Staff(Integer.parseInt(p[0]), p[1], p[2], p[3], p[4], p[5], Boolean.parseBoolean(p[6]));
    }

    // StaffInt4Salon delegation
    @Override
    public List<Booking> viewUpcomingAppointments(int staffId) {
        if (delegateManager != null) return delegateManager.viewUpcomingAppointments(staffId);
        throw new UnsupportedOperationException("StaffManager delegate required for appointment listing");
    }

    @Override
    public boolean completeAppointment(int staffId, int bookingId) {
        if (delegateManager != null) return delegateManager.completeAppointment(staffId, bookingId);
        throw new UnsupportedOperationException("StaffManager delegate required for appointment completion");
    }

    @Override
    public List<Booking> viewCompletedWork(int staffId) {
        if (delegateManager != null) return delegateManager.viewCompletedWork(staffId);
        throw new UnsupportedOperationException("StaffManager delegate required for completed work listing");
    }

    @Override
    public boolean changePassword(int staffId, String oldPassword, String newPassword) {
        if (delegateManager != null) return delegateManager.changePassword(staffId, oldPassword, newPassword);
        if (checkPassword(oldPassword)) {
            changePassword(newPassword);
            return true;
        }
        return false;
    }
}
