package in.edu.tint.it.salon.manager;

import in.edu.tint.it.salon.DataStore;
import in.edu.tint.it.salon.model.Payment;
import java.util.List;
import java.util.Vector;
import java.util.stream.Collectors;

/**
 * Business manager for payment records and transactions.
 * Manages Vector<Payment> adhering to the lab architectural specifications.
 */
public class PaymentManager {
    private final DataStore dataStore;
    private final Vector<Payment> payments;

    public PaymentManager(DataStore dataStore) {
        this.dataStore = dataStore;
        this.payments = dataStore.payments;
    }

    public Vector<Payment> getAllPayments() {
        return payments;
    }

    public List<Payment> getPaymentsByCustomer(int customerId) {
        return payments.stream()
                .filter(p -> p.getCustomerId() == customerId)
                .collect(Collectors.toList());
    }

    public Payment getPaymentByBookingId(int bookingId) {
        for (Payment p : payments) {
            if (p.getBookingId() == bookingId) return p;
        }
        return null;
    }

    public Payment recordPayment(int bookingId, int customerId, double amount, String method) {
        int id = dataStore.nextPaymentId();
        Payment payment = new Payment(id, bookingId, customerId, amount, method, Payment.Status.PAID);
        payments.add(payment);
        dataStore.save();
        return payment;
    }

    public boolean refundPayment(int bookingId) {
        Payment p = getPaymentByBookingId(bookingId);
        if (p != null && p.getStatus() == Payment.Status.PAID) {
            p.setStatus(Payment.Status.REFUNDED);
            dataStore.save();
            return true;
        }
        return false;
    }
}
