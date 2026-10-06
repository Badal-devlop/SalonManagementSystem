package in.edu.tint.it.salon.model;

/**
 * POJO / JavaBean representing a payment record linked to a booking.
 */
public class Payment {
    public enum Status { PAID, REFUNDED }

    private int id;
    private int bookingId;
    private int customerId;
    private double amount;
    private String method;
    private Status status;

    public Payment() {
    }

    public Payment(int id, int bookingId, int customerId, double amount, String method, Status status) {
        this.id = id;
        this.bookingId = bookingId;
        this.customerId = customerId;
        this.amount = amount;
        this.method = method;
        this.status = status;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getBookingId() { return bookingId; }
    public void setBookingId(int bookingId) { this.bookingId = bookingId; }

    public int getCustomerId() { return customerId; }
    public void setCustomerId(int customerId) { this.customerId = customerId; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public String toLine() {
        return id + "|" + bookingId + "|" + customerId + "|" + amount + "|" + method + "|" + status;
    }

    public static Payment fromLine(String line) {
        String[] p = line.split("\\|", -1);
        return new Payment(Integer.parseInt(p[0]), Integer.parseInt(p[1]), Integer.parseInt(p[2]),
                Double.parseDouble(p[3]), p[4], Status.valueOf(p[5]));
    }
}
