package io.smallrye.health;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;

import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.junit.Test;

public class HealthStatusTest {

    @Test
    public void testHealthStatusUp() {

        final HealthCheck myHealth = HealthStatus.up("myHealth");

        final HealthCheckResponse healthCheckResponse = myHealth.call();

        assertThat(healthCheckResponse.getName(), is("myHealth"));
        assertThat(healthCheckResponse.getStatus(), is(HealthCheckResponse.Status.UP));
    }

    @Test
    public void testHealthStatusDown() {

        final HealthCheck myHealth = HealthStatus.down("myHealth");

        final HealthCheckResponse healthCheckResponse = myHealth.call();

        assertThat(healthCheckResponse.getName(), is("myHealth"));
        assertThat(healthCheckResponse.getStatus(), is(HealthCheckResponse.Status.DOWN));
    }

    @Test
    public void testRandomNameHealthStatus() {

        final HealthCheck healthCheck = HealthStatus.status(true);

        final HealthCheckResponse healthCheckResponse = healthCheck.call();

        assertThat(healthCheckResponse.getName(), startsWith("unnamed-health-check-"));
    }

    @Test
    public void testBooleanSupplierHealthStatus() {

        final HealthCheck myHealth = HealthStatus.status("myHealth", () -> true);

        final HealthCheckResponse healthCheckResponse = myHealth.call();

        assertThat(healthCheckResponse.getName(), is("myHealth"));
        assertThat(healthCheckResponse.getStatus(), is(HealthCheckResponse.Status.UP));

    }

    @Test
    public void testBooleanHealthStatus() {

        final HealthCheck myHealth = HealthStatus.status("myHealth", true);

        final HealthCheckResponse healthCheckResponse = myHealth.call();

        assertThat(healthCheckResponse.getName(), is("myHealth"));
        assertThat(healthCheckResponse.getStatus(), is(HealthCheckResponse.Status.UP));

    }

}
