package salon;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/** Common base for the three entities: Customer, Staff and Admin. */
public abstract class Person {
    private final int id;
    private String name;
    private String phone;
    private final String username;
    private String passwordHash;

    protected Person(int id, String name, String phone, String username, String passwordHash) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.username = username;
        this.passwordHash = passwordHash;
    }

    public abstract String getRole();

    /** One line of text used to save this object to a file. */
    public abstract String toLine();

    public int getId() { return id; }
    public String getName() { return name; }
    public String getPhone() { return phone; }
    public String getUsername() { return username; }

    public void setName(String name) { this.name = name; }
    public void setPhone(String phone) { this.phone = phone; }

    public boolean checkPassword(String raw) {
        return passwordHash.equals(hash(raw));
    }

    public void changePassword(String raw) {
        this.passwordHash = hash(raw);
    }

    protected String baseLine() {
        return id + "|" + name + "|" + phone + "|" + username + "|" + passwordHash;
    }

    public static String hash(String raw) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(raw.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
