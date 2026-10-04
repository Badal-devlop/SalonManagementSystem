package salon;

public class Staff extends Person {

    private String specialization;
    private boolean active;

    public Staff(int id, String name, String phone, String username,
            String passwordHash, String specialization, boolean active) {

        super(id, name, phone, username, passwordHash);

        this.specialization = specialization;
        this.active = active;
    }

    @Override
    public String getRole() {
        return "Staff";
    }

    public String getSpecialization() {
        return specialization;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public String toLine() {
        String data = baseLine();
        data = data + "|" + specialization;
        data = data + "|" + active;

        return data;
    }

    public static Staff fromLine(String line) {

        String[] parts = line.split("\\|", -1);

        int id = Integer.parseInt(parts[0]);
        String name = parts[1];
        String phone = parts[2];
        String username = parts[3];
        String passwordHash = parts[4];
        String specialization = parts[5];
        boolean active = Boolean.parseBoolean(parts[6]);

        return new Staff(
                id,
                name,
                phone,
                username,
                passwordHash,
                specialization,
                active);
    }
}