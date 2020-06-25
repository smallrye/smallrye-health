package io.smallrye.health;

import java.util.concurrent.ThreadLocalRandom;
import java.util.function.BooleanSupplier;

import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;

public class HealthStatus {

    /**
     * Creates a health check with status up.
     * 
     * @param name of the health check
     * @return Health check with status up and given name.
     */
    public static HealthCheck up(String name) {
        return status(name, true);
    }

    /**
     * Creates a health check with status down.
     * 
     * @param name of the health check
     * @return Health check with status down and given name.
     */
    public static HealthCheck down(String name) {
        return status(name, false);
    }

    /**
     * Creates a health check with status set from supplier and default health check name (health-check).
     * 
     * @param supplier to get status.
     * @return Health check with given status and default name.
     */
    public static HealthCheck status(BooleanSupplier supplier) {
        return status(supplier.getAsBoolean());
    }

    /**
     * Creates a health check with given status and default health check name (health-check).
     * 
     * @param status
     * @return Health check with given status and default name.
     */
    public static HealthCheck status(boolean status) {
        return status(generateRandomHealthCheckName(), status);
    }

    /**
     * Creates a health check with given status and health check name.
     * 
     * @param name of the status.
     * @param supplier to get status.
     * @return Health check with given status and name.
     */
    public static HealthCheck status(String name, BooleanSupplier supplier) {
        return status(name, supplier.getAsBoolean());
    }

    /**
     * Creates a health check with given status and health check name.
     * 
     * @param name of the status.
     * @param status
     * @return Health check with given status and name.
     */
    public static HealthCheck status(String name, boolean status) {
        return () -> HealthCheckResponse.named(name).status(status).build();
    }

    private static final String generateRandomHealthCheckName() {
        int suffix = ThreadLocalRandom.current().nextInt(99999);
        return String.format("unnamed-health-check-%05d", suffix);
    }

}
