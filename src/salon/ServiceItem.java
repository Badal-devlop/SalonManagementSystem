package salon;

/** A service offered by the salon. */
public class ServiceItem {
    private final int id;
    private String name;
    private double price;
    private int durationHours;
    private String requiredSpecialization;
    private boolean active;

    public ServiceItem(int id, String name, double price, int durationHours,
                       String requiredSpecialization, boolean active) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.durationHours = durationHours;
        this.requiredSpecialization = requiredSpecialization;
        this.active = active;
    }

    // Backward-compatible constructor for older code/data.
    public ServiceItem(int id, String name, double price, boolean active) {
        this(id, name, price, 1, "Any", active);
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public double getPrice() { return price; }
    public int getDurationHours() { return durationHours; }
    public String getRequiredSpecialization() { return requiredSpecialization; }
    public boolean isActive() { return active; }

    public void setPrice(double price) { this.price = price; }
    public void setDurationHours(int durationHours) { this.durationHours = durationHours; }
    public void setRequiredSpecialization(String requiredSpecialization) {
        this.requiredSpecialization = requiredSpecialization;
    }
    public void setActive(boolean active) { this.active = active; }

    public boolean canBePerformedBy(Staff staff) {
        if (requiredSpecialization.equalsIgnoreCase("Any")) return true;
        String[] options = requiredSpecialization.split("[,/]");
        for (String option : options) {
            if (staff.getSpecialization().trim().equalsIgnoreCase(option.trim())) return true;
        }
        return false;
    }

    public String toLine() {
        return id + "|" + name + "|" + price + "|" + durationHours + "|"
                + requiredSpecialization + "|" + active;
    }

    public static ServiceItem fromLine(String line) {
        String[] p = line.split("\\|", -1);
        // Supports the previous 4-column format: id|name|price|active
        if (p.length == 4) {
            return new ServiceItem(Integer.parseInt(p[0]), p[1], Double.parseDouble(p[2]),
                    1, "Any", Boolean.parseBoolean(p[3]));
        }
        return new ServiceItem(Integer.parseInt(p[0]), p[1], Double.parseDouble(p[2]),
                Integer.parseInt(p[3]), p[4], Boolean.parseBoolean(p[5]));
    }
}
