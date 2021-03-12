package io.smallrye.health;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertThrows;

import java.util.Arrays;

import javax.json.JsonArray;
import javax.json.JsonObject;

import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import io.smallrye.health.api.AsyncHealthCheck;
import io.smallrye.health.registry.LivenessHealthRegistry;
import io.smallrye.health.registry.ReadinessHealthRegistry;
import io.smallrye.mutiny.Uni;

public class HealthRegistryTest {

    private LivenessHealthRegistry livenessHealthRegistry;
    private ReadinessHealthRegistry readinessHealthRegistry;
    private AsyncHealthCheckFactory asyncHealthCheckFactory;
    private SmallRyeHealthReporter reporter;

    @Before
    public void before() {
        livenessHealthRegistry = new LivenessHealthRegistry();
        readinessHealthRegistry = new ReadinessHealthRegistry();
        asyncHealthCheckFactory = new AsyncHealthCheckFactory();
        reporter = new SmallRyeHealthReporter();
        asyncHealthCheckFactory.uncheckedExceptionDataStyle = "rootCause";
        livenessHealthRegistry.setAsyncHealthCheckFactory(asyncHealthCheckFactory);
        readinessHealthRegistry.setAsyncHealthCheckFactory(asyncHealthCheckFactory);
        reporter.setEmptyChecksOutcome(HealthCheckResponse.Status.UP.toString());
        reporter.livenessHealthRegistry = livenessHealthRegistry;
        reporter.readinessHealthRegistry = readinessHealthRegistry;
        reporter.timeoutSeconds = 60;
    }

    @Test
    public void testProgrammaticRegisterRemovalWithoutIdsTest() {
        HealthCheck livenessCheck = () -> HealthCheckResponse.up("live1");
        HealthCheck readinessCheck = () -> HealthCheckResponse.up("ready1");
        AsyncHealthCheck asyncLivenessCheck = () -> Uni.createFrom().item(HealthCheckResponse.up("asyncLive1"));
        AsyncHealthCheck asyncReadinessCheck = () -> Uni.createFrom().item(HealthCheckResponse.up("asyncReady1"));

        livenessHealthRegistry.register(livenessCheck);
        readinessHealthRegistry.register(readinessCheck);
        livenessHealthRegistry.register(asyncLivenessCheck);
        readinessHealthRegistry.register(asyncReadinessCheck);

        SmallRyeHealth health = reporter.getHealth();
        assertThat(health.isDown(), is(false));
        assertThat(health.getPayload().getString("status"), is("UP"));
        assertThat(health.getPayload().getJsonArray("checks").size(), is(4));
        assertChecks(health.getPayload().getJsonArray("checks"), "live1", "asyncLive1", "ready1", "asyncReady1");

        SmallRyeHealth liveness = reporter.getLiveness();
        assertThat(liveness.isDown(), is(false));
        assertThat(liveness.getPayload().getString("status"), is("UP"));
        assertThat(liveness.getPayload().getJsonArray("checks").size(), is(2));
        assertChecks(liveness.getPayload().getJsonArray("checks"), "live1", "asyncLive1");

        SmallRyeHealth readiness = reporter.getReadiness();
        assertThat(readiness.isDown(), is(false));
        assertThat(readiness.getPayload().getString("status"), is("UP"));
        assertThat(readiness.getPayload().getJsonArray("checks").size(), is(2));
        assertChecks(readiness.getPayload().getJsonArray("checks"), "ready1", "asyncReady1");

        livenessHealthRegistry.remove(livenessCheck);
        readinessHealthRegistry.remove(readinessCheck);
        livenessHealthRegistry.remove(asyncLivenessCheck);
        readinessHealthRegistry.remove(asyncReadinessCheck);

        health = reporter.getHealth();
        assertThat(health.isDown(), is(false));
        assertThat(health.getPayload().getString("status"), is("UP"));
        assertThat(health.getPayload().getJsonArray("checks").isEmpty(), is(true));

        liveness = reporter.getLiveness();
        assertThat(liveness.isDown(), is(false));
        assertThat(liveness.getPayload().getString("status"), is("UP"));
        assertThat(liveness.getPayload().getJsonArray("checks").isEmpty(), is(true));

        readiness = reporter.getReadiness();
        assertThat(readiness.isDown(), is(false));
        assertThat(readiness.getPayload().getString("status"), is("UP"));
        assertThat(readiness.getPayload().getJsonArray("checks").isEmpty(), is(true));
    }

    @Test
    public void testProgrammaticRegisterRemovalWithIdsTest() {
        HealthCheck livenessCheck = () -> HealthCheckResponse.up("live1");
        HealthCheck readinessCheck = () -> HealthCheckResponse.up("ready1");
        AsyncHealthCheck asyncLivenessCheck = () -> Uni.createFrom().item(HealthCheckResponse.up("asyncLive1"));
        AsyncHealthCheck asyncReadinessCheck = () -> Uni.createFrom().item(HealthCheckResponse.up("asyncReady1"));

        livenessHealthRegistry.register("l1", livenessCheck);
        readinessHealthRegistry.register("r1", readinessCheck);
        livenessHealthRegistry.register("al1", asyncLivenessCheck);
        readinessHealthRegistry.register("ar1", asyncReadinessCheck);

        SmallRyeHealth health = reporter.getHealth();
        assertThat(health.isDown(), is(false));
        assertThat(health.getPayload().getString("status"), is("UP"));
        assertThat(health.getPayload().getJsonArray("checks").size(), is(4));
        assertChecks(health.getPayload().getJsonArray("checks"), "live1", "asyncLive1", "ready1", "asyncReady1");

        SmallRyeHealth liveness = reporter.getLiveness();
        assertThat(liveness.isDown(), is(false));
        assertThat(liveness.getPayload().getString("status"), is("UP"));
        assertThat(liveness.getPayload().getJsonArray("checks").size(), is(2));
        assertChecks(liveness.getPayload().getJsonArray("checks"), "live1", "asyncLive1");

        SmallRyeHealth readiness = reporter.getReadiness();
        assertThat(readiness.isDown(), is(false));
        assertThat(readiness.getPayload().getString("status"), is("UP"));
        assertThat(readiness.getPayload().getJsonArray("checks").size(), is(2));
        assertChecks(readiness.getPayload().getJsonArray("checks"), "ready1", "asyncReady1");

        livenessHealthRegistry.remove("l1");
        readinessHealthRegistry.remove("r1");
        livenessHealthRegistry.remove("al1");
        readinessHealthRegistry.remove("ar1");

        health = reporter.getHealth();
        assertThat(health.isDown(), is(false));
        assertThat(health.getPayload().getString("status"), is("UP"));
        assertThat(health.getPayload().getJsonArray("checks").isEmpty(), is(true));

        liveness = reporter.getLiveness();
        assertThat(liveness.isDown(), is(false));
        assertThat(liveness.getPayload().getString("status"), is("UP"));
        assertThat(liveness.getPayload().getJsonArray("checks").isEmpty(), is(true));

        readiness = reporter.getReadiness();
        assertThat(readiness.isDown(), is(false));
        assertThat(readiness.getPayload().getString("status"), is("UP"));
        assertThat(readiness.getPayload().getJsonArray("checks").isEmpty(), is(true));
    }

    @Test
    public void removeNonExistentTest() {
        assertThrows(IllegalStateException.class, () -> livenessHealthRegistry.remove("does-not-exist"));
        assertThrows(IllegalStateException.class, () -> readinessHealthRegistry.remove("does-not-exist"));

        assertThrows(IllegalStateException.class,
                () -> livenessHealthRegistry.remove(() -> HealthCheckResponse.up("does-not-exist")));
        assertThrows(IllegalStateException.class,
                () -> readinessHealthRegistry.remove(() -> Uni.createFrom().item(HealthCheckResponse.up("does-not-exist"))));
    }

    private void assertChecks(JsonArray checksArray, String... checkNames) {
        for (JsonObject check : checksArray.getValuesAs(JsonObject.class)) {
            if (Arrays.stream(checkNames).noneMatch(name -> name.equals(check.getString("name")))) {
                Assert.fail("Received unexpected health check " + check.getString("name"));
            }
            assertThat(check.getString("status"), is("UP"));
        }
    }
}
