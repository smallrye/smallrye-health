package io.smallrye.health.checks;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;

import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.HealthCheckResponseBuilder;

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
public class HeapMemoryHealthCheck implements HealthCheck {

    double maxPercentage = 0.9; // Default 90%

    public HeapMemoryHealthCheck() {
    }

    public HeapMemoryHealthCheck(double maxPercentage) {
        this.maxPercentage = maxPercentage;
    }

    @Override
    public HealthCheckResponse call() {
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        long memUsed = memoryBean.getHeapMemoryUsage().getUsed();
        long memMax = memoryBean.getHeapMemoryUsage().getMax();

        HealthCheckResponseBuilder responseBuilder = HealthCheckResponse.named("heap-memory")
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
