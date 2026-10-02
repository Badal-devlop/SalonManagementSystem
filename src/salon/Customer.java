package salon;

public class Customer extends Person {

    public Customer(int id, String name, String phone, String username, String passwordHash) {
        super(id, name, phone, username, passwordHash);
    }

    @Override
    public String getRole() { return "Customer"; }

    @Override
    public String toLine() { return baseLine(); }

    public static Customer fromLine(String line) {
        String[] p = line.split("\\|", -1);
        return new Customer(Integer.parseInt(p[0]), p[1], p[2], p[3], p[4]);
    }
}
