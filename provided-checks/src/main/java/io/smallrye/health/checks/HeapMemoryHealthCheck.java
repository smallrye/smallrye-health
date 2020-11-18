package io.smallrye.health.checks;

import java.lang.management.MemoryMXBean;

import org.eclipse.microprofile.health.HealthCheckResponse;

/**
 * Health check implementation that is checking heap memory usage against available heap memory
 *
 * <pre>
 * {@code
 * &#64;Produces
 * &#64;ApplicationScoped
 * &#64;Liveness
 * HealthCheck check1() {
 *   return new HeapMemoryHealthCheck();
 * }
 * }
 * </pre>
 */
public class HeapMemoryHealthCheck extends AbstractHeapMemoryHealthCheck {

    public HeapMemoryHealthCheck() {
        super();
    }

    public HeapMemoryHealthCheck(double maxPercentage) {
        super(maxPercentage);
    }

    @Override
    String name() {
        return "heap-memory";
    }

    @Override
    public HealthCheckResponse call() {
        return getHealthCheckResponse(MemoryMXBean::getHeapMemoryUsage);
    }
}
