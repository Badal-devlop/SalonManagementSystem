package in.edu.tint.it.salon;

import in.edu.tint.it.salon.model.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Creates printable text invoices for salon bookings.
 */
public final class InvoiceGenerator {
    private static final Path INVOICE_DIR = Paths.get("invoices");

    private InvoiceGenerator() { }

    public static String generate(DataStore db, Booking booking) throws IOException {
        Payment payment = db.paymentByBookingId(booking.getId());
        if (payment == null) {
            throw new IllegalStateException("No payment record exists for this booking.");
        }

        Customer customer = null;
        for (Customer c : db.customers) {
            if (c.getId() == booking.getCustomerId()) {
                customer = c;
                break;
            }
        }
        if (customer == null) {
            throw new IllegalStateException("Customer details could not be found.");
        }

        ServiceItem service = db.serviceById(booking.getServiceId());
        Staff staff = db.staffById(booking.getStaffId());

        String serviceName = service == null ? "Service (ID " + booking.getServiceId() + ")" : service.getName();
        String staffName = staff == null ? "Staff (ID " + booking.getStaffId() + ")" : staff.getName();
        String time = String.format("%02d:00", booking.getHour());

        StringBuilder bill = new StringBuilder();
        bill.append("==================================================\n");
        bill.append("             TECHNO SALON MANAGEMENT SYSTEM       \n");
        bill.append("                    INVOICE / BILL                \n");
        bill.append("==================================================\n");
        bill.append(String.format("Invoice No.       : INV-%04d%n", booking.getId()));
        bill.append("Generated On      : ")
                .append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")))
                .append("\n");
        bill.append("--------------------------------------------------\n");
        bill.append("CUSTOMER DETAILS\n");
        bill.append("Name              : ").append(customer.getName()).append("\n");
        bill.append("Phone             : ").append(customer.getPhone()).append("\n");
        bill.append("--------------------------------------------------\n");
        bill.append("APPOINTMENT DETAILS\n");
        bill.append("Booking ID        : ").append(booking.getId()).append("\n");
        bill.append("Service           : ").append(serviceName).append("\n");
        bill.append("Staff             : ").append(staffName).append("\n");
        bill.append("Appointment Date  : ").append(booking.getDate()).append("\n");
        bill.append("Appointment Time  : ").append(time).append("\n");
        bill.append("Booking Status    : ").append(booking.getStatus()).append("\n");
        bill.append("--------------------------------------------------\n");
        bill.append(String.format("%-30s Rs. %10.2f%n", "Service Amount", booking.getPrice()));
        bill.append(String.format("%-30s Rs. %10.2f%n", "Total Amount", payment.getAmount()));
        bill.append("Payment Method    : ").append(payment.getMethod()).append("\n");
        bill.append("Payment Status    : ").append(payment.getStatus()).append("\n");
        bill.append("--------------------------------------------------\n");
        bill.append("             Thank you for visiting!              \n");
        bill.append("==================================================\n");

        Files.createDirectories(INVOICE_DIR);
        Path file = INVOICE_DIR.resolve(String.format("Invoice_%04d.txt", booking.getId()));
        Files.writeString(file, bill.toString(), StandardCharsets.UTF_8);
        return bill.toString();
    }
}
