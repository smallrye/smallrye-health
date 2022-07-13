package io.smallrye.health;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Arrays;

import javax.json.JsonArray;
import javax.json.JsonObject;

import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.smallrye.health.api.AsyncHealthCheck;
import io.smallrye.health.api.HealthRegistry;
import io.smallrye.health.api.HealthType;
import io.smallrye.health.registry.HealthRegistries;
import io.smallrye.mutiny.Uni;

public class HealthRegistryTest {

    private HealthRegistry livenessHealthRegistry;
    private HealthRegistry readinessHealthRegistry;
    private HealthRegistry wellnessHealthRegistry;
    private HealthRegistry startupHealthRegistry;
    private SmallRyeHealthReporter reporter;

    @BeforeEach
    public void before() {
        livenessHealthRegistry = HealthRegistries.getRegistry(HealthType.LIVENESS);
        readinessHealthRegistry = HealthRegistries.getRegistry(HealthType.READINESS);
        wellnessHealthRegistry = HealthRegistries.getRegistry(HealthType.WELLNESS);
        startupHealthRegistry = HealthRegistries.getRegistry(HealthType.STARTUP);
        reporter = new SmallRyeHealthReporter();
    }

    @Test
    public void testLivenessProgrammaticRegisterRemovalWithoutIdsTest() {
        HealthCheck livenessCheck = () -> HealthCheckResponse.up("live");
        AsyncHealthCheck asyncLivenessCheck = () -> Uni.createFrom().item(HealthCheckResponse.up("asyncLive"));

        livenessHealthRegistry.register(livenessCheck);
        livenessHealthRegistry.register(asyncLivenessCheck);

        assertExpectedHealth(reporter.getHealth(), "live", "asyncLive");
        assertExpectedHealth(reporter.getLiveness(), "live", "asyncLive");

        livenessHealthRegistry.remove(livenessCheck);
        livenessHealthRegistry.remove(asyncLivenessCheck);

        assertExpectedHealth(reporter.getHealth());
        assertExpectedHealth(reporter.getLiveness());
    }

    @Test
    public void testReadinessProgrammaticRegisterRemovalWithoutIdsTest() {
        HealthCheck readinessCheck = () -> HealthCheckResponse.up("ready");
        AsyncHealthCheck asyncReadinessCheck = () -> Uni.createFrom().item(HealthCheckResponse.up("asyncReady"));

        readinessHealthRegistry.register(readinessCheck);
        readinessHealthRegistry.register(asyncReadinessCheck);

        assertExpectedHealth(reporter.getHealth(), "ready", "asyncReady");
        assertExpectedHealth(reporter.getReadiness(), "ready", "asyncReady");

        readinessHealthRegistry.remove(readinessCheck);
        readinessHealthRegistry.remove(asyncReadinessCheck);

        assertExpectedHealth(reporter.getHealth());
        assertExpectedHealth(reporter.getReadiness());
    }

    @Test
    public void testWellnessProgrammaticRegisterRemovalWithoutIdsTest() {
        HealthCheck wellnessCheck = () -> HealthCheckResponse.up("wellness");
        AsyncHealthCheck asyncWellnessCheck = () -> Uni.createFrom().item(HealthCheckResponse.up("asyncWellness"));

        wellnessHealthRegistry.register(wellnessCheck);
        wellnessHealthRegistry.register(asyncWellnessCheck);

        assertExpectedHealth(reporter.getHealth(), "wellness", "asyncWellness");
        assertExpectedHealth(reporter.getWellness(), "wellness", "asyncWellness");

        wellnessHealthRegistry.remove(wellnessCheck);
        wellnessHealthRegistry.remove(asyncWellnessCheck);

        assertExpectedHealth(reporter.getHealth());
        assertExpectedHealth(reporter.getReadiness());
    }

    @Test
    public void testStartupProgrammaticRegisterRemovalWithoutIdsTest() {
        HealthCheck startupCheck = () -> HealthCheckResponse.up("startup");
        AsyncHealthCheck asyncStartupCheck = () -> Uni.createFrom().item(HealthCheckResponse.up("asyncStartup"));

        startupHealthRegistry.register(startupCheck);
        startupHealthRegistry.register(asyncStartupCheck);

        assertExpectedHealth(reporter.getHealth(), "startup", "asyncStartup");
        assertExpectedHealth(reporter.getStartup(), "startup", "asyncStartup");

        startupHealthRegistry.remove(startupCheck);
        startupHealthRegistry.remove(asyncStartupCheck);

        assertExpectedHealth(reporter.getHealth());
        assertExpectedHealth(reporter.getStartup());
    }

    @Test
    public void testLivenessProgrammaticRegisterRemovalWithIdsTest() {
        HealthCheck livenessCheck = () -> HealthCheckResponse.up("liveness");
        AsyncHealthCheck asyncLivenessCheck = () -> Uni.createFrom().item(HealthCheckResponse.up("asyncLiveness"));

        livenessHealthRegistry.register("l1", livenessCheck);
        livenessHealthRegistry.register("al1", asyncLivenessCheck);

        assertExpectedHealth(reporter.getHealth(), "liveness", "asyncLiveness");
        assertExpectedHealth(reporter.getLiveness(), "liveness", "asyncLiveness");

        livenessHealthRegistry.remove("l1");
        livenessHealthRegistry.remove("al1");

        assertExpectedHealth(reporter.getHealth());
        assertExpectedHealth(reporter.getLiveness());
    }

    @Test
    public void testReadinessProgrammaticRegisterRemovalWithIdsTest() {
        HealthCheck readinessCheck = () -> HealthCheckResponse.up("readiness");
        AsyncHealthCheck asyncReadinessCheck = () -> Uni.createFrom().item(HealthCheckResponse.up("asyncReadiness"));

        readinessHealthRegistry.register("r1", readinessCheck);
        readinessHealthRegistry.register("ar1", asyncReadinessCheck);

        assertExpectedHealth(reporter.getHealth(), "readiness", "asyncReadiness");
        assertExpectedHealth(reporter.getReadiness(), "readiness", "asyncReadiness");

        readinessHealthRegistry.remove("r1");
        readinessHealthRegistry.remove("ar1");

        assertExpectedHealth(reporter.getHealth());
        assertExpectedHealth(reporter.getReadiness());
    }

    @Test
    public void testWellnessProgrammaticRegisterRemovalWithIdsTest() {
        HealthCheck wellnessCheck = () -> HealthCheckResponse.up("wellness");
        AsyncHealthCheck asyncWellnessCheck = () -> Uni.createFrom().item(HealthCheckResponse.up("asyncWellness"));

        wellnessHealthRegistry.register("w1", wellnessCheck);
        wellnessHealthRegistry.register("aw1", asyncWellnessCheck);

        assertExpectedHealth(reporter.getHealth(), "wellness", "asyncWellness");
        assertExpectedHealth(reporter.getWellness(), "wellness", "asyncWellness");

        wellnessHealthRegistry.remove("w1");
        wellnessHealthRegistry.remove("aw1");

        assertExpectedHealth(reporter.getHealth());
        assertExpectedHealth(reporter.getWellness());
    }

    @Test
    public void testStartupProgrammaticRegisterRemovalWithIdsTest() {
        HealthCheck startupCheck = () -> HealthCheckResponse.up("startup");
        AsyncHealthCheck asyncStartupCheck = () -> Uni.createFrom().item(HealthCheckResponse.up("asyncStartup"));

        startupHealthRegistry.register("s1", startupCheck);
        startupHealthRegistry.register("as1", asyncStartupCheck);

        assertExpectedHealth(reporter.getHealth(), "startup", "asyncStartup");
        assertExpectedHealth(reporter.getStartup(), "startup", "asyncStartup");

        startupHealthRegistry.remove("s1");
        startupHealthRegistry.remove("as1");

        assertExpectedHealth(reporter.getHealth());
        assertExpectedHealth(reporter.getStartup());
    }

    @Test
    public void testHealthGroupProgrammaticRegisterRemovalWithIdsTest() {
        HealthCheck testCheck = () -> HealthCheckResponse.up("test");
        AsyncHealthCheck asyncTestCheck = () -> Uni.createFrom().item(HealthCheckResponse.up("asyncTest"));

        String groupName = "test-group";
        HealthRegistry testGroupRegistry = HealthRegistries.getHealthGroupRegistry(groupName);

        testGroupRegistry.register("t1", testCheck);
        testGroupRegistry.register("at1", asyncTestCheck);

        assertExpectedHealth(reporter.getHealthGroup(groupName), "test", "asyncTest");
        assertExpectedHealth(reporter.getHealthGroups(), "test", "asyncTest");

        testGroupRegistry.remove("t1");
        testGroupRegistry.remove("at1");

        assertExpectedHealth(reporter.getHealthGroup(groupName));
        assertExpectedHealth(reporter.getHealthGroups());
    }

    @Test
    public void removeNonExistentTest() {
        assertThrows(IllegalStateException.class, () -> livenessHealthRegistry.remove("does-not-exist"));
        assertThrows(IllegalStateException.class, () -> readinessHealthRegistry.remove("does-not-exist"));
        assertThrows(IllegalStateException.class, () -> wellnessHealthRegistry.remove("does-not-exist"));
        assertThrows(IllegalStateException.class, () -> startupHealthRegistry.remove("does-not-exist"));

        assertThrows(IllegalStateException.class,
                () -> livenessHealthRegistry.remove(() -> HealthCheckResponse.up("does-not-exist")));
        assertThrows(IllegalStateException.class,
                () -> readinessHealthRegistry.remove(() -> Uni.createFrom().item(HealthCheckResponse.up("does-not-exist"))));
        assertThrows(IllegalStateException.class,
                () -> wellnessHealthRegistry.remove(() -> Uni.createFrom().item(HealthCheckResponse.up("does-not-exist"))));
        assertThrows(IllegalStateException.class,
                () -> startupHealthRegistry.remove(() -> Uni.createFrom().item(HealthCheckResponse.up("does-not-exist"))));
    }

    @Test
    public void healthGroupNullTest() {
        assertThrows(IllegalArgumentException.class, () -> HealthRegistries.getHealthGroupRegistry(null));
    }

    @Test
    public void testMaxGroupRegistriesCreations() {
        // default defined at HealthRegistries#MAX_GROUP_REGISTRIES_COUNT_DEFAULT = 100
        // one group already created by previous test

        for (int i = 0; i < 99; i++) {
            HealthRegistries.getHealthGroupRegistry("healthGroup" + i);
        }

        assertEquals(100, HealthRegistries.getHealthGroupRegistries().size());

        assertThrows(IllegalStateException.class,
                () -> HealthRegistries.getHealthGroupRegistry("HealthGroup101"));
    }

    private void assertExpectedHealth(SmallRyeHealth health, String... healthCheckNames) {
        assertThat(health.isDown(), is(false));
        assertThat(health.getPayload().getString("status"), is("UP"));
        if (healthCheckNames.length == 0) {
            assertThat(health.getPayload().getString("status"), is("UP"));
            assertThat(health.getPayload().getJsonArray("checks").isEmpty(), is(true));
        } else {
            assertThat(health.getPayload().getJsonArray("checks").size(), is(healthCheckNames.length));
            assertChecks(health.getPayload().getJsonArray("checks"), healthCheckNames);
        }
    }

    private void assertChecks(JsonArray checksArray, String... checkNames) {
        for (JsonObject check : checksArray.getValuesAs(JsonObject.class)) {
            if (Arrays.stream(checkNames).noneMatch(name -> name.equals(check.getString("name")))) {
                Assertions.fail("Received unexpected health check " + check.getString("name"));
            }
            assertThat(check.getString("status"), is("UP"));
        }
    }
}
