package salon;

public class Admin extends Person {

    public Admin(int id, String name, String phone, String username, String passwordHash) {
        super(id, name, phone, username, passwordHash);
    }

    @Override
    public String getRole() { return "Admin"; }

    @Override
    public String toLine() { return baseLine(); }

    public static Admin fromLine(String line) {
        String[] p = line.split("\\|", -1);
        return new Admin(Integer.parseInt(p[0]), p[1], p[2], p[3], p[4]);
    }
}
