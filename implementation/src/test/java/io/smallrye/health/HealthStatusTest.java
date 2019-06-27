package io.smallrye.health;

import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class HealthStatusTest {

    @Test
    public void testHealthStatusUp() {

        final HealthCheck myHealth = HealthStatus.up("myHealth");

        final HealthCheckResponse healthCheckResponse = myHealth.call();

        assertThat(healthCheckResponse.getName(), is("myHealth"));
        assertThat(healthCheckResponse.getState(), is(HealthCheckResponse.State.UP));
    }

    @Test
    public void testHealthStatusDown() {

        final HealthCheck myHealth = HealthStatus.down("myHealth");

        final HealthCheckResponse healthCheckResponse = myHealth.call();

        assertThat(healthCheckResponse.getName(), is("myHealth"));
        assertThat(healthCheckResponse.getState(), is(HealthCheckResponse.State.DOWN));
    }

    @Test
    public void testDefaultNameHealthStatus() {

        final HealthCheck healthCheck = HealthStatus.state(true);

        final HealthCheckResponse healthCheckResponse = healthCheck.call();

        assertThat(healthCheckResponse.getName(), is(HealthStatus.DEFAULT_HEALTH_CHECK_NAME));
    }

    @Test
    public void testBooleanSupplierHealthStatus() {

        final HealthCheck myHealth = HealthStatus.state("myHealth", () -> true);

        final HealthCheckResponse healthCheckResponse = myHealth.call();

        assertThat(healthCheckResponse.getName(), is("myHealth"));
        assertThat(healthCheckResponse.getState(), is(HealthCheckResponse.State.UP));

    }

    @Test
    public void testBooleanHealthStatus() {

        final HealthCheck myHealth = HealthStatus.state("myHealth", true);

        final HealthCheckResponse healthCheckResponse = myHealth.call();

        assertThat(healthCheckResponse.getName(), is("myHealth"));
        assertThat(healthCheckResponse.getState(), is(HealthCheckResponse.State.UP));

    }

}
