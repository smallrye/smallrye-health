package io.smallrye.health.checks;

import static org.junit.Assert.assertEquals;

import org.eclipse.microprofile.health.HealthCheckResponse;
import org.junit.Test;

public class InetAddressHealthCheckTest {

    @Test
    public void testInetAddressCheck() {
        final InetAddressHealthCheck inetAddressHealthCheck = new InetAddressHealthCheck("127.0.0.1");
        final HealthCheckResponse healthCheckResponse = inetAddressHealthCheck.call();

        assertEquals(InetAddressHealthCheck.DEFAULT_NAME, healthCheckResponse.getName());
        assertEquals("127.0.0.1", healthCheckResponse.getData().get().get("host"));
        assertEquals(HealthCheckResponse.State.UP, healthCheckResponse.getState());
    }

    @Test
    public void testInetAddressCheckNoneReachable() {
        final InetAddressHealthCheck inetAddressHealthCheck = new InetAddressHealthCheck("www.fdghreer.invalid");
        final HealthCheckResponse healthCheckResponse = inetAddressHealthCheck.call();

        assertEquals(HealthCheckResponse.State.DOWN, healthCheckResponse.getState());
    }

}
