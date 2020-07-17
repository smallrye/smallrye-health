package io.smallrye.health.checks;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;

import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.HealthCheckResponseBuilder;

/**
 * Health check implementation that is checking memory usage against available memory
 *
 * <pre>
 * {@code
 * &#64;Produces
 * &#64;ApplicationScoped
 * &#64;Liveness
 * HealthCheck check1() {
 *   return new NonHeapMemoryHealthCheck();
 * }
 * }
 * </pre>
 */
public class NonHeapMemoryHealthCheck implements HealthCheck {

    double maxPercentage = 0.9;

    public NonHeapMemoryHealthCheck() {
    }

    public NonHeapMemoryHealthCheck(double maxPercentage) {
        this.maxPercentage = maxPercentage;
    }

    @Override
    public HealthCheckResponse call() {
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        long memUsed = memoryBean.getNonHeapMemoryUsage().getUsed();
        long memMax = memoryBean.getNonHeapMemoryUsage().getMax();

        HealthCheckResponseBuilder responseBuilder = HealthCheckResponse.named("non-heap-memory")
                .withData("used", memUsed)
                .withData("max", memMax)
                .withData("max %", String.valueOf(maxPercentage));

        if (memMax > 0) {
            boolean status = (memUsed < memMax * maxPercentage);
            return responseBuilder.state(status).build();
        } else {
            // Max not available
            return responseBuilder.up().build();
        }
    }
}
