package in.edu.tint.it.salon.model;

/**
 * POJO / JavaBean representing a service offered by the salon.
 */
public class ServiceItem {
    private int id;
    private String name;
    private double price;
    private int durationHours;
    private String requiredSpecialization;
    private boolean active;

    public ServiceItem() {
    }

    public ServiceItem(int id, String name, double price, int durationHours,
                       String requiredSpecialization, boolean active) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.durationHours = durationHours;
        this.requiredSpecialization = requiredSpecialization;
        this.active = active;
    }

    public ServiceItem(int id, String name, double price, boolean active) {
        this(id, name, price, 1, "Any", active);
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public int getDurationHours() { return durationHours; }
    public void setDurationHours(int durationHours) { this.durationHours = durationHours; }

    public String getRequiredSpecialization() { return requiredSpecialization; }
    public void setRequiredSpecialization(String requiredSpecialization) {
        this.requiredSpecialization = requiredSpecialization;
    }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public boolean canBePerformedBy(Staff staff) {
        if (staff == null) return false;
        if (requiredSpecialization == null || requiredSpecialization.equalsIgnoreCase("Any")) return true;
        String[] options = requiredSpecialization.split("[,/]");
        for (String option : options) {
            if (staff.getSpecialization() != null &&
                staff.getSpecialization().trim().equalsIgnoreCase(option.trim())) {
                return true;
            }
        }
        return false;
    }

    public String toLine() {
        return id + "|" + name + "|" + price + "|" + durationHours + "|"
                + requiredSpecialization + "|" + active;
    }

    public static ServiceItem fromLine(String line) {
        String[] p = line.split("\\|", -1);
        if (p.length == 4) {
            return new ServiceItem(Integer.parseInt(p[0]), p[1], Double.parseDouble(p[2]),
                    1, "Any", Boolean.parseBoolean(p[3]));
        }
        return new ServiceItem(Integer.parseInt(p[0]), p[1], Double.parseDouble(p[2]),
                Integer.parseInt(p[3]), p[4], Boolean.parseBoolean(p[5]));
    }

    @Override
    public String toString() {
        return name + " (Rs. " + String.format("%.2f", price) + ", " + durationHours + " hr)";
    }
}
