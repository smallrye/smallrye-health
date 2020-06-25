package io.smallrye.health.checks;

import java.io.IOException;
import java.net.InetAddress;

import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.HealthCheckResponseBuilder;

/**
 * Health check implementation to check if host is reachable using {@code java.net.InetAddress.isReachable} method.
 *
 * <pre>
 * {@code
 * &#64;Produces
 * &#64;ApplicationScoped
 * &#64;Liveness
 * HealthCheck check1() {
 *   return new InetAddressHealthCheck("service.com");
 * }
 * }
 * </pre>
 *
 * @see InetAddress
 */
public class InetAddressHealthCheck implements HealthCheck {

    static final String DEFAULT_NAME = "Internet Address Check";
    static final int DEFAULT_TIMEOUT = 2000;

    private String host;
    private String name;
    private int timeout;

    public InetAddressHealthCheck(String host) {
        this.host = host;
        this.name = DEFAULT_NAME;
        this.timeout = DEFAULT_TIMEOUT;
    }

    @Override
    public HealthCheckResponse call() {
        final HealthCheckResponseBuilder healthCheckResponseBuilder = HealthCheckResponse.named(this.name);
        healthCheckResponseBuilder.withData("host", this.host);
        try {
            InetAddress addr = InetAddress.getByName(this.host);
            final boolean reachable = addr.isReachable(this.timeout);
            if (!reachable) {
                healthCheckResponseBuilder.withData("error", String.format("Host %s not reachable.", this.host));
            }

            healthCheckResponseBuilder.status(reachable);
        } catch (IOException e) {
            HealthChecksLogging.log.inetAddressHealthCheckError(e);

            healthCheckResponseBuilder.withData("error", e.getMessage());
            healthCheckResponseBuilder.down();
        }

        return healthCheckResponseBuilder.build();
    }

    /**
     * Sets timeout in millis.
     * 
     * @param timeout in millis.
     * @return InetAddressHealthCheck instance.
     */
    public InetAddressHealthCheck timeout(int timeout) {
        this.timeout = timeout;
        return this;
    }

    /**
     * Sets the name of the health check.
     * 
     * @param name of health check.
     * @return InetAddressHealthCheck instance.
     */
    public InetAddressHealthCheck name(String name) {
        this.name = name;
        return this;
    }
}
