package in.edu.tint.it.salon.model;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * POJO/JavaBean base class for Person entities (Customer, Staff, Admin).
 */
public abstract class Person {
    private int id;
    private String name;
    private String phone;
    private String username;
    private String passwordHash;

    public Person() {
    }

    public Person(int id, String name, String phone, String username, String passwordHash) {
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
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public boolean checkPassword(String raw) {
        return passwordHash != null && passwordHash.equals(hash(raw));
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
