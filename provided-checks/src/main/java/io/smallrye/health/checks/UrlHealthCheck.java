package io.smallrye.health.checks;

import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;

import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.HealthCheckResponseBuilder;

/**
 * Health check implementation to check if host is reachable using a Http URL connection.
 *
 * <pre>
 * {@code
 * &#64;Produces
 * &#64;ApplicationScoped
 * &#64;Liveness
 * HealthCheck check1() {
 *   return new UrlHealthCheck("www.google.com");
 * }
 * }
 * </pre>
 *
 * @see Socket
 */
public class UrlHealthCheck implements HealthCheck {

    static final String DEFAULT_NAME = "Url Check";
    static final int DEFAULT_TIMEOUT = 2000;
    static final String DEFAULT_REQUEST_METHOD = "GET";
    static final int DEFAULT_EXPECTED_STATUS_CODE = HttpURLConnection.HTTP_OK;

    private String url;
    private String name;
    private int timeout;
    private String requestMethod;
    private int statusCode;

    public UrlHealthCheck(String url) {
        this.url = url;
        this.requestMethod = DEFAULT_REQUEST_METHOD;
        this.statusCode = DEFAULT_EXPECTED_STATUS_CODE;
        this.name = DEFAULT_NAME;
        this.timeout = DEFAULT_TIMEOUT;
    }

    @Override
    public HealthCheckResponse call() {
        final HealthCheckResponseBuilder healthCheckResponseBuilder = HealthCheckResponse
                .named(name);
        healthCheckResponseBuilder.withData("host", String.format("%s %s", this.requestMethod, this.url));
        try {
            final HttpURLConnection httpUrlConn = (HttpURLConnection) new URL(this.url)
                    .openConnection();

            httpUrlConn.setRequestMethod(requestMethod);

            httpUrlConn.setConnectTimeout(timeout);
            httpUrlConn.setReadTimeout(timeout);

            final boolean isUp = httpUrlConn.getResponseCode() == statusCode;

            if (!isUp) {
                healthCheckResponseBuilder.withData("error", String.format("Expected response code %d but actual is %d",
                        statusCode, httpUrlConn.getResponseCode()));
            }

            healthCheckResponseBuilder.status(isUp);

        } catch (Exception e) {
            HealthChecksLogging.log.urlHealthCheckError(e);

            healthCheckResponseBuilder.withData("error",
                    String.format("%s: %s", e.getClass().getCanonicalName(), e.getMessage()));
            healthCheckResponseBuilder.down();
        }

        return healthCheckResponseBuilder.build();
    }

    /**
     * Sets the name of the health check.
     * 
     * @param name of health check.
     * @return UrlHealthCheck instance.
     */
    public UrlHealthCheck name(String name) {
        this.name = name;
        return this;
    }

    /**
     * Sets timeout in millis.
     * 
     * @param timeout in millis.
     * @return UrlHealthCheck instance.
     */
    public UrlHealthCheck timeout(int timeout) {
        this.timeout = timeout;
        return this;
    }

    /**
     * Sets the expected status code to be returned as valid.
     * 
     * @param statusCode expected.
     * @return UrlHealthCheck instance.
     */
    public UrlHealthCheck statusCode(int statusCode) {
        this.statusCode = statusCode;
        return this;
    }

    /**
     * Sets the request method to be used (ie GET, POST, PUT, ...)
     * 
     * @param requestMethod to use.
     * @return UrlHealthCheck instance.
     */
    public UrlHealthCheck requestMethod(String requestMethod) {
        this.requestMethod = requestMethod;
        return this;
    }

}
