package in.edu.tint.it.salon;

import in.edu.tint.it.salon.manager.*;
import in.edu.tint.it.salon.view.LoginFrame;

import javax.swing.*;

/**
 * Main application entry point for the Salon Management System.
 * Launches the modern Java Swing desktop interface.
 */
public class SalonApp {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                // Set system look and feel for native OS appearance
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
            }

            DataStore dataStore = new DataStore();

            // Initialize Manager hierarchy adhering to MVC architecture
            ServiceManager serviceManager = new ServiceManager(dataStore);
            PaymentManager paymentManager = new PaymentManager(dataStore);
            BookingManager bookingManager = new BookingManager(dataStore, paymentManager);
            StaffManager staffManager = new StaffManager(dataStore, bookingManager);
            CustomerManager customerManager = new CustomerManager(dataStore, serviceManager, bookingManager, paymentManager);
            AdminManager adminManager = new AdminManager(dataStore, serviceManager, staffManager, bookingManager, paymentManager, customerManager);

            // Launch Swing View
            LoginFrame loginFrame = new LoginFrame(dataStore, customerManager, staffManager,
                    adminManager, serviceManager, bookingManager, paymentManager);
            loginFrame.setVisible(true);
        });
    }
}
