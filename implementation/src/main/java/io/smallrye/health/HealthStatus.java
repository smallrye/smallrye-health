package io.smallrye.health;

import java.util.concurrent.ThreadLocalRandom;
import java.util.function.BooleanSupplier;

import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;

public class HealthStatus {

    /**
     * Creates a health check with state up.
     * 
     * @param name of the health check
     * @return Health check with state up and given name.
     */
    public static HealthCheck up(String name) {
        return state(name, true);
    }

    /**
     * Creates a health check with state down.
     * 
     * @param name of the health check
     * @return Health check with state down and given name.
     */
    public static HealthCheck down(String name) {
        return state(name, false);
    }

    /**
     * Creates a health check with state set from supplier and default health check name (health-check).
     * 
     * @param supplier to get state.
     * @return Health check with given state and default name.
     */
    public static HealthCheck state(BooleanSupplier supplier) {
        return state(supplier.getAsBoolean());
    }

    /**
     * Creates a health check with given state and default health check name (health-check).
     * 
     * @param state
     * @return Health check with given state and default name.
     */
    public static HealthCheck state(boolean state) {
        return state(generateRandomHealthCheckName(), state);
    }

    /**
     * Creates a health check with given state and health check name.
     * 
     * @param name of the state.
     * @param supplier to get state.
     * @return Health check with given state and name.
     */
    public static HealthCheck state(String name, BooleanSupplier supplier) {
        return state(name, supplier.getAsBoolean());
    }

    /**
     * Creates a health check with given state and health check name.
     * 
     * @param name of the state.
     * @param state
     * @return Health check with given state and name.
     */
    public static HealthCheck state(String name, boolean state) {
        return () -> HealthCheckResponse.named(name).state(state).build();
    }

    private static final String generateRandomHealthCheckName() {
        int suffix = ThreadLocalRandom.current().nextInt(99999);
        return String.format("unnamed-health-check-%05d", suffix);
    }

}
