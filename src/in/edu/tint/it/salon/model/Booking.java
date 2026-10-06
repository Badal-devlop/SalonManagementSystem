package in.edu.tint.it.salon.model;

import java.time.LocalDate;

/**
 * POJO / JavaBean representing an appointment booking made by a customer.
 */
public class Booking {
    public enum Status { BOOKED, COMPLETED, CANCELLED }

    private int id;
    private int customerId;
    private int staffId;
    private int serviceId;
    private LocalDate date;
    private int hour;
    private Status status;
    private double price;

    public Booking() {
    }

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
    public void setId(int id) { this.id = id; }

    public int getCustomerId() { return customerId; }
    public void setCustomerId(int customerId) { this.customerId = customerId; }

    public int getStaffId() { return staffId; }
    public void setStaffId(int staffId) { this.staffId = staffId; }

    public int getServiceId() { return serviceId; }
    public void setServiceId(int serviceId) { this.serviceId = serviceId; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public int getHour() { return hour; }
    public void setHour(int hour) { this.hour = hour; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

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
