package de.subcentral.core.util;

public class ServiceUtil {
    private ServiceUtil() {
        throw new AssertionError(getClass() + " is an utility class and therefore cannot be instantiated");
    }

    public static <T extends Service> T getService(Iterable<T> availableServices, String name) {
        for (T service : availableServices) {
            if (service.getName().equals(name)) {
                return service;
            }
        }
        return null;
    }
}
