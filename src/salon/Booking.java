package salon;

import java.time.LocalDate;

/** One appointment made by a customer. */
public class Booking {
    public enum Status { BOOKED, COMPLETED, CANCELLED }

    private final int id;
    private final int customerId;
    private final int staffId;
    private final int serviceId;
    private final LocalDate date;
    private final int hour;
    private Status status;
    private final double price;

    public Booking(int id, int customerId, int staffId, int serviceId,
                   LocalDate date, int hour, Status status, double price) {
        this.id = id;
        this.customerId = customerId;
        this.staffId = staffId;
        this.serviceId = serviceId;
        this.date = date;
        this.hour = hour;
        this.status = status;
        this.price = price;
    }

    public int getId() { return id; }
    public int getCustomerId() { return customerId; }
    public int getStaffId() { return staffId; }
    public int getServiceId() { return serviceId; }
    public LocalDate getDate() { return date; }
    public int getHour() { return hour; }
    public Status getStatus() { return status; }
    public double getPrice() { return price; }
    public void setStatus(Status status) { this.status = status; }

    public String toLine() {
        return id + "|" + customerId + "|" + staffId + "|" + serviceId + "|" + date + "|" + hour + "|" + status + "|" + price;
    }

    public static Booking fromLine(String line) {
        String[] p = line.split("\\|", -1);
        return new Booking(Integer.parseInt(p[0]), Integer.parseInt(p[1]), Integer.parseInt(p[2]),
                Integer.parseInt(p[3]), LocalDate.parse(p[4]), Integer.parseInt(p[5]),
                Status.valueOf(p[6]), Double.parseDouble(p[7]));
    }
}
