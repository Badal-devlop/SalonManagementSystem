package salon;

public class Staff extends Person {
    private String specialization;
    private boolean active;

    public Staff(int id, String name, String phone, String username, String passwordHash,
                 String specialization, boolean active) {
        super(id, name, phone, username, passwordHash);
        this.specialization = specialization;
        this.active = active;
    }

    @Override
    public String getRole() { return "Staff"; }

    public String getSpecialization() { return specialization; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    @Override
    public String toLine() {
        return baseLine() + "|" + specialization + "|" + active;
    }

    public static Staff fromLine(String line) {
        String[] p = line.split("\\|", -1);
        return new Staff(Integer.parseInt(p[0]), p[1], p[2], p[3], p[4], p[5], Boolean.parseBoolean(p[6]));
    }
}
