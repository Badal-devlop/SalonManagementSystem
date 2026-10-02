package salon;

/** Payment record linked to one booking. */
public class Payment {
    public enum Status { PAID, REFUNDED }

    private final int id;
    private final int bookingId;
    private final int customerId;
    private final double amount;
    private final String method;
    private Status status;

    public Payment(int id, int bookingId, int customerId, double amount, String method, Status status) {
        this.id = id;
        this.bookingId = bookingId;
        this.customerId = customerId;
        this.amount = amount;
        this.method = method;
        this.status = status;
    }

    public int getId() { return id; }
    public int getBookingId() { return bookingId; }
    public int getCustomerId() { return customerId; }
    public double getAmount() { return amount; }
    public String getMethod() { return method; }
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
