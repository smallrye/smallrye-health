package io.smallrye.health.checks;

import static org.junit.Assert.assertEquals;

import java.net.HttpURLConnection;

import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.junit.Test;

public class UrlHealthCheckTest {

    @Test
    public void testUrlCheck() {
        final UrlHealthCheck urlHealthCheck = new UrlHealthCheck("http://www.google.com");
        final HealthCheckResponse healthCheckResponse = urlHealthCheck.call();

        assertEquals(UrlHealthCheck.DEFAULT_NAME, healthCheckResponse.getName());
        assertEquals("GET http://www.google.com", healthCheckResponse.getData().get().get("host"));
        assertEquals(HealthCheckResponse.State.UP, healthCheckResponse.getState());

    }

    @Test
    public void testUrlCheckIncorrectStatusCode() {
        final HealthCheck urlHealthCheck = new UrlHealthCheck("http://www.google.com")
                .statusCode(HttpURLConnection.HTTP_CREATED);
        final HealthCheckResponse healthCheckResponse = urlHealthCheck.call();

        assertEquals(HealthCheckResponse.State.DOWN, healthCheckResponse.getState());
        assertEquals("Expected response code 201 but actual is 200", healthCheckResponse.getData().get().get("error"));
    }

    @Test
    public void testUrlCheckNoneExistingUrl() {
        final UrlHealthCheck urlHealthCheck = new UrlHealthCheck("http://www.fdghreer.invalid");
        final HealthCheckResponse healthCheckResponse = urlHealthCheck.call();

        assertEquals(HealthCheckResponse.State.DOWN, healthCheckResponse.getState());
        assertEquals("java.net.UnknownHostException: www.fdghreer.invalid", healthCheckResponse.getData().get().get("error"));

    }

}
