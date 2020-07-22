package io.smallrye.health.checks;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.util.function.Function;

import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.HealthCheckResponseBuilder;

public abstract class AbstractHeapMemoryHealthCheck implements HealthCheck {

    double maxPercentage = 0.9; // Default 90%

    public AbstractHeapMemoryHealthCheck() {
    }

    public AbstractHeapMemoryHealthCheck(double maxPercentage) {
        this.maxPercentage = maxPercentage;
    }

    protected HealthCheckResponse getHealthCheckResponse(Function<MemoryMXBean, MemoryUsage> memoryUsageFunction) {
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        MemoryUsage memoryUsage = memoryUsageFunction.apply(memoryBean);
        long memUsed = memoryUsage.getUsed();
        long memMax = memoryUsage.getMax();

        HealthCheckResponseBuilder responseBuilder = HealthCheckResponse.named("heap-memory")
                .withData("used", memUsed)
                .withData("max", memMax)
                .withData("max %", String.valueOf(maxPercentage));

        if (memMax > 0) {
            boolean status = (memUsed < memMax * maxPercentage);
            return responseBuilder.status(status).build();
        } else {
            // Max not available
            return responseBuilder.up().build();
        }
    }
}
