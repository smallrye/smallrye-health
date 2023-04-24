package io.smallrye.health.checks;

import java.lang.management.MemoryMXBean;

import org.eclipse.microprofile.health.HealthCheckResponse;

/**
 * Health check implementation that is checking memory usage against available memory
 *
 * <pre>
 * {@code
 * &#64;Produces
 * &#64;ApplicationScoped
 * &#64;Liveness
 * HealthCheck check1() {
 *     return new NonHeapMemoryHealthCheck();
 * }
 * }
 * </pre>
 */
public class NonHeapMemoryHealthCheck extends AbstractHeapMemoryHealthCheck {

    public NonHeapMemoryHealthCheck() {
        super();
    }

    public NonHeapMemoryHealthCheck(double maxPercentage) {
        super(maxPercentage);
    }

    @Override
    String name() {
        return "non-heap-memory";
    }

    @Override
    public HealthCheckResponse call() {
        return getHealthCheckResponse(MemoryMXBean::getNonHeapMemoryUsage);
    }
}
