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
        assertEquals(HealthCheckResponse.State.UP, healthCheckResponse.getState());

    }

    @Test
    public void testSocketHealtchCheckNoneServiceOnPort() {
        SocketHealthCheck socketHealthCheck = new SocketHealthCheck("127.0.0.1", 8985);

        final HealthCheckResponse healthCheckResponse = socketHealthCheck.call();
        assertEquals(HealthCheckResponse.State.DOWN, healthCheckResponse.getState());
        assertEquals("Connection refused (Connection refused)", healthCheckResponse.getData().get().get("error"));
    }

}
