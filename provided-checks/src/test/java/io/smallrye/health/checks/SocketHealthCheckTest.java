package io.smallrye.health.checks;

import static org.junit.Assert.assertEquals;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.eclipse.microprofile.health.HealthCheckResponse;
import org.junit.Test;

public class SocketHealthCheckTest {

    @Test
    public void testSocketHealthCheck() throws UnknownHostException {
        final String hostAddress = InetAddress.getByName("www.google.com").getHostAddress();
        SocketHealthCheck socketHealthCheck = new SocketHealthCheck(hostAddress, 80);

        final HealthCheckResponse healthCheckResponse = socketHealthCheck.call();

        assertEquals(SocketHealthCheck.DEFAULT_NAME, healthCheckResponse.getName());
        assertEquals(hostAddress + ":80", healthCheckResponse.getData().get().get("host"));
        assertEquals(HealthCheckResponse.Status.UP, healthCheckResponse.getStatus());

    }

    @Test
    public void testSocketHealthCheckNoneServiceOnPort() {
        SocketHealthCheck socketHealthCheck = new SocketHealthCheck("198.51.100.0", 8985);

        final HealthCheckResponse healthCheckResponse = socketHealthCheck.call();
        assertEquals(HealthCheckResponse.Status.DOWN, healthCheckResponse.getStatus());
        assertEquals("connect timed out", healthCheckResponse.getData().get().get("error"));
    }

}
