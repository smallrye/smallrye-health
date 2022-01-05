package io.smallrye.health;

import java.util.Map;

import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.HealthCheckResponse.Status;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class HealthCheckResponseProviderTest {

    @Test
    public void testProvider() {

        HealthCheckResponse healthStatus = HealthCheckResponse.named("test")
                .up()
                .withData("a", "b")
                .withData("c", "d")
                .build();

        Assertions.assertEquals("test", healthStatus.getName());
        Assertions.assertSame(Status.UP, healthStatus.getStatus());
        Map<String, Object> data = healthStatus.getData().get();
        Assertions.assertEquals(2, data.size());
        Assertions.assertEquals("b", data.get("a"), "Expected a");
        Assertions.assertEquals("d", data.get("c"), "Expected c");
    }

}
