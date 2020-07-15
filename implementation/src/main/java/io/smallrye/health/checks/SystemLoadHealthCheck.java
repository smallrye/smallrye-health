package io.smallrye.health.checks;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;

import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.HealthCheckResponseBuilder;

/**
 * Health check implementation that is checking average load usage against max load
 *
 * <pre>
 * {@code
 * &#64;Produces
 * &#64;ApplicationScoped
 * &#64;Liveness
 * HealthCheck check1() {
 *   return new SystemLoadHealthCheck();
 * }
 * }
 * </pre>
 */
public class SystemLoadHealthCheck implements HealthCheck {

    double max = 0.7;

    public SystemLoadHealthCheck() {
    }

    public SystemLoadHealthCheck(double max) {
        this.max = max;
    }

    @Override
    public HealthCheckResponse call() {
        OperatingSystemMXBean operatingSystemMXBean = ManagementFactory.getOperatingSystemMXBean();

        String arch = operatingSystemMXBean.getArch();
        String name = operatingSystemMXBean.getName();
        String version = operatingSystemMXBean.getVersion();
        int availableProcessors = operatingSystemMXBean.getAvailableProcessors();

        double systemLoadAverage = operatingSystemMXBean.getSystemLoadAverage();
        double systemLoadAveragePerProcessors = systemLoadAverage / availableProcessors;

        HealthCheckResponseBuilder responseBuilder = HealthCheckResponse.named("system-load")
                .withData("name", name)
                .withData("arch", arch)
                .withData("version", version)
                .withData("processors", availableProcessors)
                .withData("loadAverage", String.valueOf(systemLoadAverage))
                .withData("loadAverage per processor", String.valueOf(systemLoadAveragePerProcessors))
                .withData("loadAverage max", String.valueOf(max));

        if (systemLoadAverage > 0) {
            boolean status = systemLoadAveragePerProcessors < max;
            return responseBuilder.state(status).build();
        } else {
            // Load average not available
            return responseBuilder.up().build();
        }

    }
}
