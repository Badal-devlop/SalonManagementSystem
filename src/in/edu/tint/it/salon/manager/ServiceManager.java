package in.edu.tint.it.salon.manager;

import in.edu.tint.it.salon.DataStore;
import in.edu.tint.it.salon.model.ServiceItem;
import java.util.List;
import java.util.Vector;
import java.util.stream.Collectors;

/**
 * Business manager for Salon Service operations.
 * Manages Vector<ServiceItem> adhering to the lab architectural specifications.
 */
public class ServiceManager {
    private final DataStore dataStore;
    private final Vector<ServiceItem> services;

    public ServiceManager(DataStore dataStore) {
        this.dataStore = dataStore;
        this.services = dataStore.services;
    }

    public Vector<ServiceItem> getAllServices() {
        return services;
    }

    public List<ServiceItem> getActiveServices() {
        return services.stream().filter(ServiceItem::isActive).collect(Collectors.toList());
    }

    public List<ServiceItem> getInactiveServices() {
        return services.stream().filter(s -> !s.isActive()).collect(Collectors.toList());
    }

    public ServiceItem getServiceById(int id) {
        for (ServiceItem s : services) {
            if (s.getId() == id) return s;
        }
        return null;
    }

    public ServiceItem addService(String name, double price, int durationHours, String specialization) {
        int id = dataStore.nextServiceId();
        ServiceItem item = new ServiceItem(id, name, price, durationHours, specialization, true);
        services.add(item);
        dataStore.save();
        return item;
    }

    public boolean editService(int id, double price, int durationHours, String specialization) {
        ServiceItem s = getServiceById(id);
        if (s == null) return false;
        s.setPrice(price);
        s.setDurationHours(durationHours);
        s.setRequiredSpecialization(specialization);
        dataStore.save();
        return true;
    }

    public boolean deactivateService(int id) {
        ServiceItem s = getServiceById(id);
        if (s == null) return false;
        s.setActive(false);
        dataStore.save();
        return true;
    }

    public boolean reactivateService(int id) {
        ServiceItem s = getServiceById(id);
        if (s == null) return false;
        s.setActive(true);
        dataStore.save();
        return true;
    }
}
