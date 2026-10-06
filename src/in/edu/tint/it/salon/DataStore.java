package in.edu.tint.it.salon;

import in.edu.tint.it.salon.model.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Text-file persistence layer for the Salon Management System.
 * Maintains thread-safe Vector collections matching the instructor's architecture.
 */
public class DataStore {
    public static final int OPEN_HOUR = 10;
    public static final int CLOSE_HOUR = 18; // last start is 17:00 for a 1-hour service

    private static final Path DIR = Paths.get("data");

    public final Vector<Admin> admins = new Vector<>();
    public final Vector<Staff> staff = new Vector<>();
    public final Vector<Customer> customers = new Vector<>();
    public final Vector<ServiceItem> services = new Vector<>();
    public final Vector<Booking> bookings = new Vector<>();
    public final Vector<Payment> payments = new Vector<>();

    public DataStore() {
        load();
        seedIfEmpty();
        repairMissingPayments();
    }

    public int nextAdminId() { return admins.stream().mapToInt(Admin::getId).max().orElse(0) + 1; }
    public int nextStaffId() { return staff.stream().mapToInt(Staff::getId).max().orElse(0) + 1; }
    public int nextCustomerId() { return customers.stream().mapToInt(Customer::getId).max().orElse(0) + 1; }
    public int nextServiceId() { return services.stream().mapToInt(ServiceItem::getId).max().orElse(0) + 1; }
    public int nextBookingId() { return bookings.stream().mapToInt(Booking::getId).max().orElse(0) + 1; }
    public int nextPaymentId() { return payments.stream().mapToInt(Payment::getId).max().orElse(0) + 1; }

    public boolean usernameTaken(String username) {
        for (Admin a : admins) if (a.getUsername().equalsIgnoreCase(username)) return true;
        for (Staff s : staff) if (s.getUsername().equalsIgnoreCase(username)) return true;
        for (Customer c : customers) if (c.getUsername().equalsIgnoreCase(username)) return true;
        return false;
    }

    public Staff staffById(int id) {
        for (Staff s : staff) if (s.getId() == id) return s;
        return null;
    }

    public ServiceItem serviceById(int id) {
        for (ServiceItem s : services) if (s.getId() == id) return s;
        return null;
    }

    public Booking bookingById(int id) {
        for (Booking b : bookings) if (b.getId() == id) return b;
        return null;
    }

    public Payment paymentByBookingId(int bookingId) {
        for (Payment p : payments) if (p.getBookingId() == bookingId) return p;
        return null;
    }

    public Customer customerById(int id) {
        for (Customer c : customers) if (c.getId() == id) return c;
        return null;
    }

    public String customerName(int id) {
        Customer c = customerById(id);
        return c == null ? "(unknown)" : c.getName();
    }

    public String staffName(int id) {
        Staff s = staffById(id);
        return s == null ? "(unknown)" : s.getName();
    }

    public String serviceName(int id) {
        ServiceItem s = serviceById(id);
        return s == null ? "(unknown)" : s.getName();
    }

    public List<Staff> activeStaff() {
        return staff.stream().filter(Staff::isActive).collect(Collectors.toList());
    }

    public List<Staff> eligibleStaff(ServiceItem service) {
        if (service == null) return new ArrayList<>();
        return activeStaff().stream().filter(service::canBePerformedBy).collect(Collectors.toList());
    }

    public List<ServiceItem> activeServices() {
        return services.stream().filter(ServiceItem::isActive).collect(Collectors.toList());
    }

    /** Returns true if any part of an appointment overlaps another booked appointment. */
    public boolean slotTaken(int staffId, LocalDate date, int startHour, int durationHours) {
        int endHour = startHour + durationHours;
        for (Booking b : bookings) {
            if (b.getStaffId() != staffId || !b.getDate().equals(date)
                    || b.getStatus() != Booking.Status.BOOKED) continue;
            ServiceItem existing = serviceById(b.getServiceId());
            int existingDuration = existing == null ? 1 : existing.getDurationHours();
            int existingEnd = b.getHour() + existingDuration;
            if (startHour < existingEnd && b.getHour() < endHour) return true;
        }
        return false;
    }

    public List<Integer> freeHours(int staffId, LocalDate date, int durationHours) {
        List<Integer> free = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        for (int h = OPEN_HOUR; h + durationHours <= CLOSE_HOUR; h++) {
            boolean past = date.equals(now.toLocalDate()) && h <= now.getHour();
            if (!past && !slotTaken(staffId, date, h, durationHours)) free.add(h);
        }
        return free;
    }

    public synchronized void save() {
        try {
            Files.createDirectories(DIR);
            write("admins.txt", admins, Admin::toLine);
            write("staff.txt", staff, Staff::toLine);
            write("customers.txt", customers, Customer::toLine);
            write("services.txt", services, ServiceItem::toLine);
            write("bookings.txt", bookings, Booking::toLine);
            write("payments.txt", payments, Payment::toLine);
        } catch (IOException e) {
            System.err.println("Warning: could not save data (" + e.getMessage() + ")");
        }
    }

    private synchronized void load() {
        try {
            read("admins.txt", admins, Admin::fromLine);
            read("staff.txt", staff, Staff::fromLine);
            read("customers.txt", customers, Customer::fromLine);
            read("services.txt", services, ServiceItem::fromLine);
            read("bookings.txt", bookings, Booking::fromLine);
            read("payments.txt", payments, Payment::fromLine);
        } catch (IOException | RuntimeException e) {
            System.err.println("Warning: could not read saved data (" + e.getMessage() + ")");
        }
    }

    private <T> void write(String file, List<T> items, Function<T, String> toLine) throws IOException {
        Files.write(DIR.resolve(file), items.stream().map(toLine).collect(Collectors.toList()));
    }

    private <T> void read(String file, List<T> target, Function<String, T> fromLine) throws IOException {
        Path p = DIR.resolve(file);
        if (!Files.exists(p)) return;
        for (String line : Files.readAllLines(p)) {
            if (!line.isBlank()) target.add(fromLine.apply(line));
        }
    }

    private void seedIfEmpty() {
        boolean changed = false;
        if (admins.isEmpty()) {
            admins.add(new Admin(1, "Salon Admin", "9000000000", "admin", Person.hash("admin123")));
            changed = true;
        }
        if (staff.isEmpty()) {
            staff.add(new Staff(1, "Riya", "9000000001", "staff1", Person.hash("staff123"), "Hair Stylist", true));
            staff.add(new Staff(2, "Arjun", "9000000002", "staff2", Person.hash("staff123"), "Barber", true));
            staff.add(new Staff(3, "Neha", "9000000003", "staff3", Person.hash("staff123"), "Beautician", true));
            changed = true;
        }
        if (services.isEmpty()) {
            services.add(new ServiceItem(1, "Haircut", 300, 1, "Hair Stylist/Barber", true));
            services.add(new ServiceItem(2, "Shave", 150, 1, "Barber", true));
            services.add(new ServiceItem(3, "Facial", 800, 1, "Beautician", true));
            services.add(new ServiceItem(4, "Hair Colour", 1500, 2, "Hair Stylist", true));
            changed = true;
        }
        if (changed) save();
    }

    private void repairMissingPayments() {
        boolean changed = false;
        for (Booking b : bookings) {
            if (paymentByBookingId(b.getId()) == null && b.getStatus() != Booking.Status.CANCELLED) {
                payments.add(new Payment(nextPaymentId(), b.getId(), b.getCustomerId(), b.getPrice(), "Legacy", Payment.Status.PAID));
                changed = true;
            }
        }
        if (changed) save();
    }
}
