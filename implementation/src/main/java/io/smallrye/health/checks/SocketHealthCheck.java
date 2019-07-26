package io.smallrye.health.checks;

import java.io.IOException;
import java.net.Socket;

import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.HealthCheckResponseBuilder;

/**
 * Health check implementation to check if host is reachable using a socket.
 *
 * <pre>
 * {@code
 * &#64;Produces
 * &#64;ApplicationScoped
 * &#64;Liveness
 * HealthCheck check1() {
 *   return new SocketHealthCheck("192.168.0.2", 5432);
 * }
 * }
 * </pre>
 *
 * @see Socket
 */
public class SocketHealthCheck implements HealthCheck {

    static final String DEFAULT_NAME = "Socket Check";

    private String host;
    private String name;
    private int port;

    public SocketHealthCheck(String host, int port) {
        this.host = host;
        this.port = port;
        this.name = DEFAULT_NAME;
    }

    @Override
    public HealthCheckResponse call() {
        HealthCheckResponseBuilder healthCheckResponseBuilder = HealthCheckResponse
                .named(name);
        healthCheckResponseBuilder.withData("host", String.format("%s:%d", this.host, this.port));
        try (Socket s = new Socket(this.host, this.port)) {
            healthCheckResponseBuilder.up();
        } catch (IOException ex) {
            healthCheckResponseBuilder.withData("error", ex.getMessage());
            healthCheckResponseBuilder.down();
        }
        return healthCheckResponseBuilder.build();
    }

    /**
     * Sets the name of the health check.
     * 
     * @param name of health check.
     * @return SocketHealthCheck instance.
     */
    public SocketHealthCheck name(String name) {
        this.name = name;
        return this;
    }
}
