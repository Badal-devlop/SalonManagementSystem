package salon;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class SalonApp {
    private final DataStore db = new DataStore();

    public static void main(String[] args) {
        new SalonApp().run();
    }

    private void run() {
        System.out.println("=========================================");
        System.out.println("        SALON MANAGEMENT SYSTEM 1.0");
        System.out.println("=========================================");
        System.out.println("Default demo accounts (if unchanged):");
        System.out.println("Admin : admin / admin123");
        System.out.println("Staff : staff1 / staff123");
        System.out.println("Staff : staff2 / staff123");
        System.out.println("Staff : staff3 / staff123");

        while (true) {
            System.out.println("\n1. Customer login");
            System.out.println("2. Staff login");
            System.out.println("3. Admin login");
            System.out.println("4. Register as a new customer");
            System.out.println("0. Exit");
            int choice = Console.number("Choose: ", 0, 4);
            switch (choice) {
                case 1: {
                    Customer c = login(db.customers);
                    if (c != null) customerMenu(c);
                    break;
                }
                case 2: {
                    Staff s = login(db.staff);
                    if (s != null) staffMenu(s);
                    break;
                }
                case 3: {
                    Admin a = login(db.admins);
                    if (a != null) adminMenu(a);
                    break;
                }
                case 4: registerCustomer(); break;
                default:
                    System.out.println("Goodbye.");
                    return;
            }
        }
    }

    private <T extends Person> T login(List<T> users) {
        String username = Console.text("Username: ");
        String password = Console.text("Password: ");
        for (T u : users) {
            if (u.getUsername().equalsIgnoreCase(username) && u.checkPassword(password)) {
                if (u instanceof Staff && !((Staff) u).isActive()) {
                    System.out.println("This staff account has been deactivated.");
                    return null;
                }
                System.out.println("Welcome, " + u.getName() + ".");
                return u;
            }
        }
        System.out.println("Wrong username or password.");
        return null;
    }

    private void registerCustomer() {
        String name = Console.text("Full name: ");
        String phone = Console.phone("Phone (10 digits): ");
        String username;
        while (true) {
            username = Console.text("Choose a username: ");
            if (db.usernameTaken(username)) System.out.println("That username is taken.");
            else break;
        }
        String password = Console.password("Choose a password (min 6 characters): ");
        db.customers.add(new Customer(db.nextCustomerId(), name, phone, username, Person.hash(password)));
        db.save();
        System.out.println("Registration complete. You can log in now.");
    }

    private void changePassword(Person p) {
        String oldPw = Console.text("Current password: ");
        if (!p.checkPassword(oldPw)) {
            System.out.println("Current password is wrong.");
            return;
        }
        p.changePassword(Console.password("New password (min 6 characters): "));
        db.save();
        System.out.println("Password changed.");
    }

    // =====================================================================
    // Customer
    // =====================================================================

    private void customerMenu(Customer me) {
        while (true) {
            System.out.println("\n--- Customer Menu ---");
            System.out.println("1. View services and prices");
            System.out.println("2. Book an appointment");
            System.out.println("3. My bookings");
            System.out.println("4. Cancel a booking");
            System.out.println("5. My payments");
            System.out.println("6. Edit profile");
            System.out.println("7. Change password");
            System.out.println("0. Logout");
            switch (Console.number("Choose: ", 0, 7)) {
                case 1: printServices(db.activeServices()); break;
                case 2: bookAppointment(me); break;
                case 3: printBookings(bookingsOfCustomer(me.getId())); break;
                case 4: cancelBooking(me); break;
                case 5: printPayments(paymentsOfCustomer(me.getId())); break;
                case 6: editCustomerProfile(me); break;
                case 7: changePassword(me); break;
                default: return;
            }
        }
    }

    private void editCustomerProfile(Customer me) {
        System.out.println("\n--- Edit Profile ---");
        System.out.println("1. Change name");
        System.out.println("2. Change phone");
        System.out.println("0. Back");
        switch (Console.number("Choose: ", 0, 2)) {
            case 1:
                me.setName(Console.text("New name: "));
                db.save();
                System.out.println("Name updated.");
                break;
            case 2:
                me.setPhone(Console.phone("New phone (10 digits): "));
                db.save();
                System.out.println("Phone updated.");
                break;
            default:
                return;
        }
    }

    private void bookAppointment(Customer me) {
        List<ServiceItem> services = db.activeServices();
        if (services.isEmpty()) {
            System.out.println("No services are available right now.");
            return;
        }

        printServices(services);
        ServiceItem service = pickService(services);
        List<Staff> eligible = db.eligibleStaff(service);
        if (eligible.isEmpty()) {
            System.out.println("No active staff member is qualified for this service.");
            return;
        }

        LocalDate date = Console.futureDate("Date (yyyy-MM-dd): ", 30);

        System.out.println("\nEligible staff:");
        for (Staff s : eligible) {
            System.out.println("  " + s.getId() + ". " + s.getName() + " (" + s.getSpecialization() + ")");
        }

        Staff chosen = null;
        while (chosen == null) {
            int id = Console.number("Staff ID: ", 1, Integer.MAX_VALUE);
            Staff s = db.staffById(id);
            if (s != null && s.isActive() && service.canBePerformedBy(s)) chosen = s;
            else System.out.println("Pick an eligible staff ID from the list.");
        }

        List<Integer> free = db.freeHours(chosen.getId(), date, service.getDurationHours());
        if (free.isEmpty()) {
            System.out.println(chosen.getName() + " has no free slots for this service on " + date + ".");
            return;
        }

        System.out.println("\nFree start times on " + date + " for " + service.getDurationHours() + " hour(s):");
        for (int h : free) System.out.println("  " + timeText(h) + " - " + timeText(h + service.getDurationHours()));

        int hour;
        while (true) {
            hour = Console.number("Start hour (24h, e.g. 14): ", DataStore.OPEN_HOUR, DataStore.CLOSE_HOUR - 1);
            if (free.contains(hour)) break;
            System.out.println("That slot is not in the free list.");
        }

        System.out.println("\nBooking summary:");
        System.out.println("Service : " + service.getName());
        System.out.println("Staff   : " + chosen.getName());
        System.out.println("Date    : " + date);
        System.out.println("Time    : " + timeText(hour) + " - " + timeText(hour + service.getDurationHours()));
        System.out.printf("Price   : Rs. %.2f%n", service.getPrice());

        if (!Console.confirm("Continue to payment")) {
            System.out.println("Booking not made.");
            return;
        }

        String method = paymentMethod();
        if (!Console.confirm("Confirm payment of Rs. " + String.format("%.2f", service.getPrice()) + " by " + method)) {
            System.out.println("Booking cancelled before payment.");
            return;
        }

        // Final availability check before saving the booking.
        if (db.slotTaken(chosen.getId(), date, hour, service.getDurationHours())) {
            System.out.println("That slot was just taken. Please choose another slot.");
            return;
        }

        Booking b = new Booking(db.nextBookingId(), me.getId(), chosen.getId(), service.getId(),
                date, hour, Booking.Status.BOOKED, service.getPrice());
        db.bookings.add(b);
        db.payments.add(new Payment(db.nextPaymentId(), b.getId(), me.getId(),
                b.getPrice(), method, Payment.Status.PAID));
        db.save();
        System.out.println("Payment successful.");
        System.out.println("Booking confirmed. Your booking ID is " + b.getId() + ".");
    }

    private String paymentMethod() {
        System.out.println("\nPayment method:");
        System.out.println("1. UPI");
        System.out.println("2. Card");
        System.out.println("3. Cash");
        switch (Console.number("Choose: ", 1, 3)) {
            case 1: return "UPI";
            case 2: return "Card";
            default: return "Cash";
        }
    }

    private void cancelBooking(Customer me) {
        List<Booking> open = bookingsOfCustomer(me.getId()).stream()
                .filter(b -> b.getStatus() == Booking.Status.BOOKED)
                .collect(Collectors.toList());
        if (open.isEmpty()) {
            System.out.println("You have no upcoming bookings to cancel.");
            return;
        }
        printBookings(open);
        int id = Console.number("Booking ID to cancel (0 to go back): ", 0, Integer.MAX_VALUE);
        if (id == 0) return;
        Booking b = db.bookingById(id);
        if (b == null || !open.contains(b)) {
            System.out.println("That is not one of your upcoming bookings.");
            return;
        }
        if (!isBookingInFuture(b)) {
            System.out.println("An appointment that has already started cannot be cancelled.");
            return;
        }
        if (Console.confirm("Cancel booking " + id)) {
            b.setStatus(Booking.Status.CANCELLED);
            Payment payment = db.paymentByBookingId(b.getId());
            if (payment != null && payment.getStatus() == Payment.Status.PAID) {
                payment.setStatus(Payment.Status.REFUNDED);
            }
            db.save();
            System.out.println("Booking cancelled. Payment marked as refunded.");
        }
    }

    private List<Booking> bookingsOfCustomer(int customerId) {
        return db.bookings.stream()
                .filter(b -> b.getCustomerId() == customerId)
                .sorted(byDateTime())
                .collect(Collectors.toList());
    }

    private List<Payment> paymentsOfCustomer(int customerId) {
        return db.payments.stream()
                .filter(p -> p.getCustomerId() == customerId)
                .collect(Collectors.toList());
    }

    // =====================================================================
    // Staff
    // =====================================================================

    private void staffMenu(Staff me) {
        while (true) {
            System.out.println("\n--- Staff Menu ---");
            System.out.println("1. My upcoming appointments");
            System.out.println("2. Mark an appointment as completed");
            System.out.println("3. My completed work");
            System.out.println("4. Change password");
            System.out.println("0. Logout");
            switch (Console.number("Choose: ", 0, 4)) {
                case 1: printBookings(staffBookings(me, Booking.Status.BOOKED)); break;
                case 2: completeBooking(me); break;
                case 3: printBookings(staffBookings(me, Booking.Status.COMPLETED)); break;
                case 4: changePassword(me); break;
                default: return;
            }
        }
    }

    private List<Booking> staffBookings(Staff me, Booking.Status status) {
        return db.bookings.stream()
                .filter(b -> b.getStaffId() == me.getId() && b.getStatus() == status)
                .sorted(byDateTime())
                .collect(Collectors.toList());
    }

    private void completeBooking(Staff me) {
        List<Booking> due = staffBookings(me, Booking.Status.BOOKED).stream()
                .filter(this::appointmentFinished)
                .collect(Collectors.toList());
        if (due.isEmpty()) {
            System.out.println("Nothing is ready to complete yet. The appointment's end time must have passed.");
            return;
        }
        printBookings(due);
        int id = Console.number("Booking ID to mark completed (0 to go back): ", 0, Integer.MAX_VALUE);
        if (id == 0) return;
        Booking b = db.bookingById(id);
        if (b == null || !due.contains(b)) {
            System.out.println("That booking is not in your completed-ready list.");
            return;
        }
        b.setStatus(Booking.Status.COMPLETED);
        db.save();
        System.out.println("Marked as completed.");
    }

    // =====================================================================
    // Admin
    // =====================================================================

    private void adminMenu(Admin me) {
        while (true) {
            System.out.println("\n--- Admin Menu ---");
            System.out.println("1. Manage services");
            System.out.println("2. Manage staff");
            System.out.println("3. View customers");
            System.out.println("4. View all bookings");
            System.out.println("5. View payments");
            System.out.println("6. Revenue report");
            System.out.println("7. Change password");
            System.out.println("0. Logout");
            switch (Console.number("Choose: ", 0, 7)) {
                case 1: manageServices(); break;
                case 2: manageStaff(); break;
                case 3: viewCustomers(); break;
                case 4: printBookings(db.bookings.stream().sorted(byDateTime()).collect(Collectors.toList())); break;
                case 5: printPayments(db.payments); break;
                case 6: revenueReport(); break;
                case 7: changePassword(me); break;
                default: return;
            }
        }
    }

    private void manageServices() {
        while (true) {
            System.out.println("\n-- Services --");
            System.out.println("1. List all");
            System.out.println("2. Add");
            System.out.println("3. Edit price/duration/specialization");
            System.out.println("4. Deactivate");
            System.out.println("5. Reactivate");
            System.out.println("0. Back");
            switch (Console.number("Choose: ", 0, 5)) {
                case 1: printServices(db.services); break;
                case 2: addService(); break;
                case 3: editService(); break;
                case 4: deactivateService(); break;
                case 5: reactivateService(); break;
                default: return;
            }
        }
    }

    private void addService() {
        String name = Console.text("Service name: ");
        double price = Console.amount("Price (Rs.): ");
        int duration = Console.number("Duration in hours (1-4): ", 1, 4);
        String specialization = Console.text("Required specialization (e.g. Hair Stylist, Barber or Any): ");
        db.services.add(new ServiceItem(db.nextServiceId(), name, price, duration, specialization, true));
        db.save();
        System.out.println("Service added.");
    }

    private void editService() {
        List<ServiceItem> list = db.activeServices();
        if (list.isEmpty()) { System.out.println("No active services."); return; }
        printServices(list);
        ServiceItem s = pickService(list);
        s.setPrice(Console.amount("New price (Rs.): "));
        s.setDurationHours(Console.number("New duration in hours (1-4): ", 1, 4));
        s.setRequiredSpecialization(Console.text("Required specialization: "));
        db.save();
        System.out.println("Service updated. Existing bookings keep their original price and duration.");
    }

    private void deactivateService() {
        List<ServiceItem> list = db.activeServices();
        if (list.isEmpty()) { System.out.println("No active services."); return; }
        printServices(list);
        ServiceItem s = pickService(list);
        if (Console.confirm("Deactivate " + s.getName())) {
            s.setActive(false);
            db.save();
            System.out.println("Service deactivated. Old bookings remain visible.");
        }
    }

    private void reactivateService() {
        List<ServiceItem> list = db.services.stream().filter(s -> !s.isActive()).collect(Collectors.toList());
        if (list.isEmpty()) { System.out.println("No inactive services."); return; }
        printServices(list);
        ServiceItem s = pickService(list);
        s.setActive(true);
        db.save();
        System.out.println("Service reactivated.");
    }

    private void manageStaff() {
        while (true) {
            System.out.println("\n-- Staff --");
            System.out.println("1. List all");
            System.out.println("2. Add");
            System.out.println("3. Deactivate");
            System.out.println("4. Reactivate");
            System.out.println("0. Back");
            switch (Console.number("Choose: ", 0, 4)) {
                case 1: listStaff(); break;
                case 2: addStaff(); break;
                case 3: deactivateStaff(); break;
                case 4: reactivateStaff(); break;
                default: return;
            }
        }
    }

    private void listStaff() {
        if (db.staff.isEmpty()) { System.out.println("No staff members."); return; }
        System.out.printf("%-4s %-16s %-12s %-16s %-8s%n", "ID", "Name", "Phone", "Specialization", "Status");
        for (Staff s : db.staff) {
            System.out.printf("%-4d %-16s %-12s %-16s %-8s%n", s.getId(), s.getName(), s.getPhone(),
                    s.getSpecialization(), s.isActive() ? "Active" : "Inactive");
        }
    }

    private void addStaff() {
        String name = Console.text("Name: ");
        String phone = Console.phone("Phone (10 digits): ");
        String spec = Console.text("Specialization: ");
        String username;
        while (true) {
            username = Console.text("Username: ");
            if (db.usernameTaken(username)) System.out.println("That username is taken.");
            else break;
        }
        String password = Console.password("Temporary password (min 6 characters): ");
        db.staff.add(new Staff(db.nextStaffId(), name, phone, username, Person.hash(password), spec, true));
        db.save();
        System.out.println("Staff member added. They can now use Staff login with the credentials you created.");
    }

    private void deactivateStaff() {
        List<Staff> list = db.activeStaff();
        if (list.isEmpty()) { System.out.println("No active staff."); return; }
        for (Staff s : list) System.out.println("  " + s.getId() + ". " + s.getName() + " (" + s.getSpecialization() + ")");
        Staff s = db.staffById(Console.number("Staff ID: ", 1, Integer.MAX_VALUE));
        if (s == null || !s.isActive()) { System.out.println("Not an active staff ID."); return; }

        boolean hasFuture = db.bookings.stream().anyMatch(b -> b.getStaffId() == s.getId()
                && b.getStatus() == Booking.Status.BOOKED && isBookingInFuture(b));
        if (hasFuture) {
            System.out.println(s.getName() + " still has future booked appointments. Resolve them first.");
            return;
        }
        if (Console.confirm("Deactivate " + s.getName())) {
            s.setActive(false);
            db.save();
            System.out.println("Staff member deactivated.");
        }
    }

    private void reactivateStaff() {
        List<Staff> list = db.staff.stream().filter(s -> !s.isActive()).collect(Collectors.toList());
        if (list.isEmpty()) { System.out.println("No inactive staff."); return; }
        for (Staff s : list) System.out.println("  " + s.getId() + ". " + s.getName() + " (" + s.getSpecialization() + ")");
        Staff s = db.staffById(Console.number("Staff ID: ", 1, Integer.MAX_VALUE));
        if (s == null || s.isActive()) { System.out.println("Not an inactive staff ID."); return; }
        s.setActive(true);
        db.save();
        System.out.println("Staff member reactivated.");
    }

    private void viewCustomers() {
        if (db.customers.isEmpty()) {
            System.out.println("No customers registered yet.");
            return;
        }
        System.out.printf("%-4s %-20s %-12s %-16s%n", "ID", "Name", "Phone", "Username");
        for (Customer c : db.customers) {
            System.out.printf("%-4d %-20s %-12s %-16s%n", c.getId(), c.getName(), c.getPhone(), c.getUsername());
        }
    }

    private void revenueReport() {
        List<Booking> done = db.bookings.stream()
                .filter(b -> b.getStatus() == Booking.Status.COMPLETED)
                .collect(Collectors.toList());
        double total = done.stream().mapToDouble(Booking::getPrice).sum();
        System.out.println("\nCompleted appointments: " + done.size());
        System.out.printf("Total revenue: Rs. %.2f%n", total);
        System.out.println("\nBy staff member:");
        for (Staff s : db.staff) {
            long count = done.stream().filter(b -> b.getStaffId() == s.getId()).count();
            double sum = done.stream().filter(b -> b.getStaffId() == s.getId()).mapToDouble(Booking::getPrice).sum();
            System.out.printf("  %-16s %3d appointments   Rs. %.2f%n", s.getName(), count, sum);
        }
    }

    // =====================================================================
    // Display and utility helpers
    // =====================================================================

    private void printServices(List<ServiceItem> list) {
        if (list.isEmpty()) {
            System.out.println("No services available.");
            return;
        }
        System.out.printf("%n%-4s %-18s %10s %-8s %-20s %-8s%n",
                "ID", "Service", "Price", "Hours", "Specialization", "Status");
        for (ServiceItem s : list) {
            System.out.printf("%-4d %-18s %10.2f %-8d %-20s %-8s%n", s.getId(), s.getName(), s.getPrice(),
                    s.getDurationHours(), s.getRequiredSpecialization(), s.isActive() ? "Active" : "Inactive");
        }
    }

    private ServiceItem pickService(List<ServiceItem> list) {
        while (true) {
            int id = Console.number("Service ID: ", 1, Integer.MAX_VALUE);
            for (ServiceItem s : list) if (s.getId() == id) return s;
            System.out.println("Pick an ID from the list.");
        }
    }

    private void printBookings(List<Booking> list) {
        if (list.isEmpty()) {
            System.out.println("No bookings to show.");
            return;
        }
        System.out.printf("%n%-4s %-11s %-13s %-16s %-16s %-14s %-10s %9s%n",
                "ID", "Date", "Time", "Service", "Customer", "Staff", "Status", "Price");
        for (Booking b : list) {
            ServiceItem service = db.serviceById(b.getServiceId());
            int duration = service == null ? 1 : service.getDurationHours();
            String time = timeText(b.getHour()) + "-" + timeText(b.getHour() + duration);
            System.out.printf("%-4d %-11s %-13s %-16s %-16s %-14s %-10s %9.2f%n",
                    b.getId(), b.getDate(), time, db.serviceName(b.getServiceId()),
                    db.customerName(b.getCustomerId()), db.staffName(b.getStaffId()),
                    b.getStatus(), b.getPrice());
        }
    }

    private void printPayments(List<Payment> list) {
        if (list.isEmpty()) {
            System.out.println("No payments to show.");
            return;
        }
        System.out.printf("%n%-4s %-9s %-20s %-10s %-12s %-10s%n",
                "ID", "Booking", "Customer", "Amount", "Method", "Status");
        for (Payment p : list) {
            System.out.printf("%-4d %-9d %-20s %10.2f %-12s %-10s%n",
                    p.getId(), p.getBookingId(), db.customerName(p.getCustomerId()),
                    p.getAmount(), p.getMethod(), p.getStatus());
        }
    }

    private Comparator<Booking> byDateTime() {
        return Comparator.comparing(Booking::getDate).thenComparingInt(Booking::getHour);
    }

    private String timeText(int hour) {
        return String.format("%02d:00", hour);
    }

    private boolean isBookingInFuture(Booking b) {
        ServiceItem service = db.serviceById(b.getServiceId());
        int duration = service == null ? 1 : service.getDurationHours();
        LocalDateTime end = b.getDate().atTime(b.getHour(), 0).plusHours(duration);
        return end.isAfter(LocalDateTime.now());
    }

    private boolean appointmentFinished(Booking b) {
        ServiceItem service = db.serviceById(b.getServiceId());
        int duration = service == null ? 1 : service.getDurationHours();
        LocalDateTime end = b.getDate().atTime(b.getHour(), 0).plusHours(duration);
        return !end.isAfter(LocalDateTime.now());
    }
}
