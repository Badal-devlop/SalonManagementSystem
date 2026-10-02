package salon;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

/** Small helper for reading validated input from the keyboard. */
public final class Console {
    private static final Scanner IN = new Scanner(System.in);

    private Console() { }

    public static String text(String prompt) {
        while (true) {
            System.out.print(prompt);
            String s = IN.nextLine().trim().replace("|", "/");
            if (!s.isEmpty()) return s;
            System.out.println("Input cannot be empty.");
        }
    }

    public static int number(String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt);
            try {
                int v = Integer.parseInt(IN.nextLine().trim());
                if (v >= min && v <= max) return v;
            } catch (NumberFormatException ignored) { }
            System.out.println("Enter a number between " + min + " and " + max + ".");
        }
    }

    public static double amount(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                double v = Double.parseDouble(IN.nextLine().trim());
                if (v > 0) return v;
            } catch (NumberFormatException ignored) { }
            System.out.println("Enter an amount greater than 0.");
        }
    }

    public static String phone(String prompt) {
        while (true) {
            System.out.print(prompt);
            String s = IN.nextLine().trim();
            if (s.matches("\\d{10}")) return s;
            System.out.println("Phone number must be exactly 10 digits.");
        }
    }

    public static String password(String prompt) {
        while (true) {
            System.out.print(prompt);
            String s = IN.nextLine().trim();
            if (s.length() >= 6 && !s.contains("|")) return s;
            System.out.println("Password must be at least 6 characters and must not contain '|'.");
        }
    }

    public static LocalDate futureDate(String prompt, int maxDaysAhead) {
        while (true) {
            System.out.print(prompt);
            try {
                LocalDate d = LocalDate.parse(IN.nextLine().trim());
                LocalDate today = LocalDate.now();
                if (d.isBefore(today)) {
                    System.out.println("That date has already passed.");
                } else if (d.isAfter(today.plusDays(maxDaysAhead))) {
                    System.out.println("Bookings open only " + maxDaysAhead + " days ahead.");
                } else {
                    return d;
                }
            } catch (DateTimeParseException e) {
                System.out.println("Use the format yyyy-MM-dd, for example " + LocalDate.now().plusDays(1) + ".");
            }
        }
    }

    public static boolean confirm(String prompt) {
        while (true) {
            System.out.print(prompt + " (y/n): ");
            String s = IN.nextLine().trim().toLowerCase();
            if (s.equals("y")) return true;
            if (s.equals("n")) return false;
        }
    }
}
