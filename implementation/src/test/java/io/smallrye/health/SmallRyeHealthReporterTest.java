package io.smallrye.health;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayOutputStream;
import java.time.Duration;
import java.util.function.Supplier;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.logging.StreamHandler;

import javax.json.JsonArray;
import javax.json.JsonObject;

import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.HealthCheckResponse.Status;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.smallrye.health.api.AsyncHealthCheck;
import io.smallrye.health.registry.LivenessHealthRegistry;
import io.smallrye.health.registry.ReadinessHealthRegistry;
import io.smallrye.health.registry.StartupHealthRegistry;
import io.smallrye.health.registry.WellnessHealthRegistry;
import io.smallrye.mutiny.Uni;

public class SmallRyeHealthReporterTest {

    private static final Duration maxDuration = Duration.ofSeconds(5);
    private SmallRyeHealthReporter reporter;
    private AsyncHealthCheckFactory asyncHealthCheckFactory;

    public static class FailingHealthCheck implements HealthCheck {
        @Override
        public HealthCheckResponse call() {
            throw new RuntimeException("this health check has failed");
        }
    }

    public static class DownHealthCheck implements HealthCheck {
        @Override
        public HealthCheckResponse call() {
            return HealthCheckResponse.named("down").down().build();
        }
    }

    public static class UpHealthCheck implements HealthCheck {
        @Override
        public HealthCheckResponse call() {
            return HealthCheckResponse.named("up").up().build();
        }
    }

    public static class FailingAsyncHealthCheck implements AsyncHealthCheck {
        @Override
        public Uni<HealthCheckResponse> call() {
            return Uni.createFrom().failure(new RuntimeException("this health check has failed"));
        }
    }

    public static class DownAsyncHealthCheck implements AsyncHealthCheck {
        @Override
        public Uni<HealthCheckResponse> call() {
            return Uni.createFrom().item(HealthCheckResponse.named("down").down().build());
        }
    }

    public static class UpAsyncHealthCheck implements AsyncHealthCheck {
        @Override
        public Uni<HealthCheckResponse> call() {
            return Uni.createFrom().item(HealthCheckResponse.named("up").up().build());
        }
    }

    public static class NullHealthCheck implements HealthCheck {
        @Override
        public HealthCheckResponse call() {
            return null;
        }
    }

    public static class NullAsyncHealthCheck implements AsyncHealthCheck {
        @Override
        public Uni<HealthCheckResponse> call() {
            return null;
        }
    }

    public static class NullUniAsyncHealthCheck implements AsyncHealthCheck {
        @Override
        public Uni<HealthCheckResponse> call() {
            return Uni.createFrom().item(null);
        }
    }

    @BeforeEach
    public void createReporter() {
        reporter = new SmallRyeHealthReporter();
        reporter.emptyChecksOutcome = "UP";
        reporter.timeoutSeconds = 300;

        reporter.livenessHealthRegistry = new LivenessHealthRegistry();
        reporter.readinessHealthRegistry = new ReadinessHealthRegistry();
        reporter.wellnessHealthRegistry = new WellnessHealthRegistry();
        reporter.startupHealthRegistry = new StartupHealthRegistry();

        asyncHealthCheckFactory = new AsyncHealthCheckFactory();
        asyncHealthCheckFactory.uncheckedExceptionDataStyle = "rootCause";
        reporter.asyncHealthCheckFactory = asyncHealthCheckFactory;
    }

    @Test
    public void testDefaultGetHealth() {
        testDefaultGetHealth(() -> reporter.getHealth());
    }

    @Test
    public void testDefaultGetHealthAsync() {
        testDefaultGetHealth(() -> reporter.getHealthAsync().await().atMost(maxDuration));
    }

    @Test
    public void testGetHealthWithEmptyChecksOutcomeDown() {
        testGetHealthWithEmptyChecksOutcomeDown(() -> reporter.getHealth());
    }

    @Test
    public void testGetHealthWithEmptyChecksOutcomeDownAsync() {
        testGetHealthWithEmptyChecksOutcomeDown(() -> reporter.getHealthAsync().await().atMost(maxDuration));
    }

    @Test
    public void testGetHealthWithFailingCheckAndStyleDefault() {
        testGetHealthWithFailingCheckAndStyleDefault(FailingHealthCheck.class.getName(), () -> {
            reporter.addHealthCheck(new FailingHealthCheck());

            return reporter.getHealth();
        });
    }

    @Test
    public void testGetHealthWithFailingCheckAndStyleDefaultAsync() {
        testGetHealthWithFailingCheckAndStyleDefault(FailingAsyncHealthCheck.class.getName(), () -> {
            reporter.addHealthCheck(new FailingAsyncHealthCheck());

            return reporter.getHealthAsync().await().atMost(maxDuration);
        });
    }

    @Test
    public void testGetHealthWithFailingCheckAndStyleNone() {
        testGetHealthWithFailingCheckAndStyleNone(FailingHealthCheck.class.getName(), () -> {
            reporter.addHealthCheck(new FailingHealthCheck());

            return reporter.getHealth();
        });
    }

    @Test
    public void testGetHealthWithFailingCheckAndStyleNoneAsync() {
        testGetHealthWithFailingCheckAndStyleNone(FailingAsyncHealthCheck.class.getName(), () -> {
            reporter.addHealthCheck(new FailingAsyncHealthCheck());

            return reporter.getHealthAsync().await().atMost(maxDuration);
        });
    }

    @Test
    public void testGetHealthWithFailingCheckAndStyleStackTrace() {
        testGetHealthWithFailingCheckAndStyleStackTrace(FailingHealthCheck.class.getName(), () -> {
            reporter.addHealthCheck(new FailingHealthCheck());

            return reporter.getHealth();
        });
    }

    @Test
    public void testGetHealthWithFailingCheckAndStyleStackTraceAsync() {
        testGetHealthWithFailingCheckAndStyleStackTrace(FailingAsyncHealthCheck.class.getName(), () -> {
            reporter.addHealthCheck(new FailingAsyncHealthCheck());

            return reporter.getHealthAsync().await().atMost(maxDuration);
        });
    }

    @Test
    public void testGetHealthWithMixedChecksAndStyleDefault() {
        testGetHealthWithMixedChecksAndStyleDefault(FailingHealthCheck.class.getName(), () -> {
            reporter.addHealthCheck(new UpHealthCheck());
            reporter.addHealthCheck(new FailingHealthCheck());
            reporter.addHealthCheck(new DownHealthCheck());

            return reporter.getHealth();
        });
    }

    @Test
    public void testGetHealthWithMixedChecksAndStyleDefaultAsync() {
        testGetHealthWithMixedChecksAndStyleDefault(FailingAsyncHealthCheck.class.getName(), () -> {
            reporter.addHealthCheck(new UpAsyncHealthCheck());
            reporter.addHealthCheck(new FailingAsyncHealthCheck());
            reporter.addHealthCheck(new DownAsyncHealthCheck());

            return reporter.getHealthAsync().await().atMost(maxDuration);
        });
    }

    @Test
    public void testGetHealthWithMixedChecksAndStyleNone() {
        testGetHealthWithMixedChecksAndStyleNone(FailingHealthCheck.class.getName(), () -> {
            reporter.addHealthCheck(new UpHealthCheck());
            reporter.addHealthCheck(new FailingHealthCheck());
            reporter.addHealthCheck(new DownHealthCheck());

            return reporter.getHealth();
        });
    }

    @Test
    public void testGetHealthWithMixedChecksAndStyleNoneAsync() {
        testGetHealthWithMixedChecksAndStyleNone(FailingAsyncHealthCheck.class.getName(), () -> {
            reporter.addHealthCheck(new UpAsyncHealthCheck());
            reporter.addHealthCheck(new FailingAsyncHealthCheck());
            reporter.addHealthCheck(new DownAsyncHealthCheck());

            return reporter.getHealthAsync().await().atMost(maxDuration);
        });
    }

    @Test
    public void testGetHealthWithMixedChecksAndStyleStackTrace() {
        testGetHealthWithMixedChecksAndStyleStackTrace(FailingHealthCheck.class.getName(), () -> {
            reporter.addHealthCheck(new UpHealthCheck());
            reporter.addHealthCheck(new FailingHealthCheck());
            reporter.addHealthCheck(new DownHealthCheck());

            return reporter.getHealth();
        });
    }

    @Test
    public void testGetHealthWithMixedChecksAndStyleStackTraceAsync() {
        testGetHealthWithMixedChecksAndStyleStackTrace(FailingAsyncHealthCheck.class.getName(), () -> {
            reporter.addHealthCheck(new UpAsyncHealthCheck());
            reporter.addHealthCheck(new FailingAsyncHealthCheck());
            reporter.addHealthCheck(new DownAsyncHealthCheck());

            return reporter.getHealthAsync().await().atMost(maxDuration);
        });
    }

    @Test
    public void testReportWhenDown() {
        testReport(() -> reporter.addHealthCheck(new DownHealthCheck()),
                () -> reporter.getHealth(), () -> reporter.getHealth().getPayload().toString());
    }

    @Test
    public void testReportWhenDownAsync() {
        testReport(() -> reporter.addHealthCheck(new DownAsyncHealthCheck()),
                () -> reporter.getHealthAsync().await().atMost(maxDuration),
                () -> reporter.getHealthAsync().await().atMost(maxDuration).getPayload().toString());
    }

    @Test
    public void testReportWhenException() {
        testReport(() -> reporter.addHealthCheck(new FailingHealthCheck()),
                () -> reporter.getHealth(), () -> "this health check has failed");
    }

    @Test
    public void testReportWhenExceptionAsync() {
        testReport(() -> reporter.addHealthCheck(new FailingAsyncHealthCheck()),
                () -> reporter.getHealthAsync().await().atMost(maxDuration),
                () -> "this health check has failed");
    }

    @Test
    public void testReportWhenNull() {
        testReport(() -> reporter.addHealthCheck(new NullHealthCheck()),
                () -> reporter.getHealth(), () -> HealthMessages.msg.healthCheckNull().getMessage());
    }

    @Test
    public void testReportWhenNullAsync() {
        testReport(() -> reporter.addHealthCheck(new NullAsyncHealthCheck()),
                () -> reporter.getHealthAsync().await().atMost(maxDuration),
                () -> "The supplier returned `null`");
    }

    @Test
    public void testReportWhenNullUniAsync() {
        testReport(() -> reporter.addHealthCheck(new NullUniAsyncHealthCheck()),
                () -> reporter.getHealthAsync().await().atMost(maxDuration),
                () -> "`supplier` must not be `null`");
    }

    @Test
    public void testAdditionalChecks() {
        UpHealthCheck upHealthCheck = new UpHealthCheck();
        UpAsyncHealthCheck upAsyncHealthCheck = new UpAsyncHealthCheck();
        DownHealthCheck downHealthCheck = new DownHealthCheck();
        DownAsyncHealthCheck downAsyncHealthCheck = new DownAsyncHealthCheck();

        // 1 UP sync
        reporter.addHealthCheck(upHealthCheck);
        SmallRyeHealth health = reporter.getHealth();
        SmallRyeHealth healthAsync = reporter.getHealthAsync().await().atMost(maxDuration);

        assertEquals(health, healthAsync);
        assertThat(health.isDown(), is(false));
        assertThat(health.getPayload().getString("status"), is("UP"));

        JsonArray checks = health.getPayload().getJsonArray("checks");
        assertThat(checks.size(), is(1));

        JsonObject healthCheck = checks.getJsonObject(0);
        assertThat(healthCheck.getString("name"), is("up"));
        assertThat(healthCheck.getString("status"), is("UP"));

        // 1 UP sync, 1 UP async
        reporter.addHealthCheck(upAsyncHealthCheck);
        health = reporter.getHealth();
        healthAsync = reporter.getHealthAsync().await().atMost(maxDuration);

        assertEquals(health, healthAsync);
        assertThat(health.isDown(), is(false));
        assertThat(health.getPayload().getString("status"), is("UP"));

        checks = health.getPayload().getJsonArray("checks");
        assertThat(checks.size(), is(2));

        for (JsonObject check : checks.getValuesAs(JsonObject.class)) {
            assertThat(check.getString("name"), is("up"));
            assertThat(check.getString("status"), is("UP"));
        }

        // 1 UP sync, 1 UP async, 1 DOWN sync
        reporter.addHealthCheck(downHealthCheck);
        health = reporter.getHealth();
        healthAsync = reporter.getHealthAsync().await().atMost(maxDuration);

        assertEquals(health, healthAsync);
        assertThat(health.isDown(), is(true));
        assertThat(health.getPayload().getString("status"), is("DOWN"));

        checks = health.getPayload().getJsonArray("checks");
        assertThat(checks.size(), is(3));

        for (JsonObject check : checks.getValuesAs(JsonObject.class)) {
            if (check.getString("name").equals("up")) {
                assertThat(check.getString("status"), is("UP"));
            } else if (check.getString("name").equals("down")) {
                assertThat(check.getString("status"), is("DOWN"));
            } else {
                Assertions.fail("Health returned unexpected health check: " + check.toString());
            }
        }

        // ! UP sync, 1 UP async, 1 DOWN sync, 1 DOWN async
        reporter.addHealthCheck(downAsyncHealthCheck);
        health = reporter.getHealth();
        healthAsync = reporter.getHealthAsync().await().atMost(maxDuration);

        assertEquals(health, healthAsync);
        assertThat(health.isDown(), is(true));
        assertThat(health.getPayload().getString("status"), is("DOWN"));

        checks = health.getPayload().getJsonArray("checks");
        assertThat(checks.size(), is(4));

        for (JsonObject check : checks.getValuesAs(JsonObject.class)) {
            if (check.getString("name").equals("up")) {
                assertThat(check.getString("status"), is("UP"));
            } else if (check.getString("name").equals("down")) {
                assertThat(check.getString("status"), is("DOWN"));
            } else {
                Assertions.fail("Health returned unexpected health check: " + check.toString());
            }
        }

        // replay the calls once again, cached Uni should be used
        health = reporter.getHealth();
        healthAsync = reporter.getHealthAsync().await().atMost(maxDuration);

        assertEquals(health, healthAsync);
        assertThat(health.isDown(), is(true));
        assertThat(health.getPayload().getString("status"), is("DOWN"));

        checks = health.getPayload().getJsonArray("checks");
        assertThat(checks.size(), is(4));

        for (JsonObject check : checks.getValuesAs(JsonObject.class)) {
            if (check.getString("name").equals("up")) {
                assertThat(check.getString("status"), is("UP"));
            } else if (check.getString("name").equals("down")) {
                assertThat(check.getString("status"), is("DOWN"));
            } else {
                Assertions.fail("Health returned unexpected health check: " + check.toString());
            }
        }

        // 1 DOWN sync, 1 DOWN async
        reporter.removeHealthCheck(upHealthCheck);
        reporter.removeHealthCheck(upAsyncHealthCheck);
        health = reporter.getHealth();
        healthAsync = reporter.getHealthAsync().await().atMost(maxDuration);

        assertEquals(health, healthAsync);
        assertThat(health.isDown(), is(true));
        assertThat(health.getPayload().getString("status"), is("DOWN"));

        checks = health.getPayload().getJsonArray("checks");
        assertThat(checks.size(), is(2));

        for (JsonObject check : checks.getValuesAs(JsonObject.class)) {
            if (check.getString("name").equals("down")) {
                assertThat(check.getString("status"), is("DOWN"));
            } else {
                Assertions.fail("Health returned unexpected health check: " + check.toString());
            }
        }

        // no additional checks
        reporter.removeHealthCheck(downHealthCheck);
        reporter.removeHealthCheck(downAsyncHealthCheck);
        health = reporter.getHealth();
        healthAsync = reporter.getHealthAsync().await().atMost(maxDuration);

        assertEquals(health, healthAsync);
        assertThat(health.isDown(), is(false));
        assertThat(health.getPayload().getString("status"), is("UP"));

        checks = health.getPayload().getJsonArray("checks");
        assertThat(checks.size(), is(0));
    }

    public void testDefaultGetHealth(Supplier<SmallRyeHealth> supplier) {
        SmallRyeHealth health = supplier.get();

        assertThat(health.isDown(), is(false));
        assertThat(health.getPayload().getString("status"), is("UP"));
        assertThat(health.getPayload().getJsonArray("checks"), is(empty()));
    }

    public void testGetHealthWithEmptyChecksOutcomeDown(Supplier<SmallRyeHealth> supplier) {
        reporter.setEmptyChecksOutcome(Status.DOWN.toString());

        SmallRyeHealth health = supplier.get();

        assertThat(health.isDown(), is(true));
        assertThat(health.getPayload().getString("status"), is("DOWN"));
        assertThat(health.getPayload().getJsonArray("checks"), is(empty()));
    }

    public void testGetHealthWithFailingCheckAndStyleDefault(String expectedCheckName, Supplier<SmallRyeHealth> supplier) {
        SmallRyeHealth health = supplier.get();

        assertThat(health.isDown(), is(true));
        assertThat(health.getPayload().getString("status"), is("DOWN"));
        assertThat(health.getPayload().getJsonArray("checks").getJsonObject(0).getString("name"),
                is(expectedCheckName));
        assertThat(health.getPayload().getJsonArray("checks").getJsonObject(0).getString("status"), is("DOWN"));
        assertThat(health.getPayload().getJsonArray("checks").getJsonObject(0).getJsonObject("data").getString("rootCause"),
                is("this health check has failed"));
    }

    public void testGetHealthWithFailingCheckAndStyleNone(String expectedCheckName, Supplier<SmallRyeHealth> supplier) {
        asyncHealthCheckFactory.setUncheckedExceptionDataStyle("NONE");

        SmallRyeHealth health = supplier.get();

        assertThat(health.isDown(), is(true));
        assertThat(health.getPayload().getString("status"), is("DOWN"));
        assertThat(health.getPayload().getJsonArray("checks").getJsonObject(0).getString("name"),
                is(expectedCheckName));
        assertThat(health.getPayload().getJsonArray("checks").getJsonObject(0).getString("status"), is("DOWN"));
        assertThat(health.getPayload().getJsonArray("checks").getJsonObject(0).getJsonObject("data"), is(nullValue()));
    }

    public void testGetHealthWithFailingCheckAndStyleStackTrace(String expectedCheckName, Supplier<SmallRyeHealth> supplier) {
        asyncHealthCheckFactory.setUncheckedExceptionDataStyle("stackTrace");

        SmallRyeHealth health = supplier.get();

        assertThat(health.isDown(), is(true));
        assertThat(health.getPayload().getString("status"), is("DOWN"));
        assertThat(health.getPayload().getJsonArray("checks").getJsonObject(0).getString("name"),
                is(expectedCheckName));
        assertThat(health.getPayload().getJsonArray("checks").getJsonObject(0).getString("status"), is("DOWN"));
        assertThat(health.getPayload().getJsonArray("checks").getJsonObject(0).getJsonObject("data").getString("stackTrace"),
                is(notNullValue()));
    }

    public void testGetHealthWithMixedChecksAndStyleDefault(String expectedCheckName, Supplier<SmallRyeHealth> supplier) {
        SmallRyeHealth health = supplier.get();

        assertThat(health.isDown(), is(true));
        assertThat(health.getPayload().getString("status"), is("DOWN"));

        JsonArray checks = health.getPayload().getJsonArray("checks");
        assertThat(checks.size(), is(3));

        for (JsonObject check : checks.getValuesAs(JsonObject.class)) {
            if (check.getString("name").equals("down")) {
                assertThat(check.getString("status"), is("DOWN"));
            } else if (check.getString("name").equals("up")) {
                assertThat(check.getString("status"), is("UP"));
            } else if (check.getString("name").equals(expectedCheckName)) {
                assertThat(check.getString("status"), is("DOWN"));
                assertThat(check.getJsonObject("data").getString("rootCause"),
                        is("this health check has failed"));
            } else {
                Assertions.fail("Health returned unexpected health check: " + check.toString());
            }
        }
    }

    public void testGetHealthWithMixedChecksAndStyleNone(String expectedCheckName, Supplier<SmallRyeHealth> supplier) {
        asyncHealthCheckFactory.setUncheckedExceptionDataStyle("NONE");

        SmallRyeHealth health = supplier.get();

        assertThat(health.isDown(), is(true));
        assertThat(health.getPayload().getString("status"), is("DOWN"));

        JsonArray checks = health.getPayload().getJsonArray("checks");
        assertThat(checks.size(), is(3));

        for (JsonObject check : checks.getValuesAs(JsonObject.class)) {
            if (check.getString("name").equals("down")) {
                assertThat(check.getString("status"), is("DOWN"));
            } else if (check.getString("name").equals("up")) {
                assertThat(check.getString("status"), is("UP"));
            } else if (check.getString("name").equals(expectedCheckName)) {
                assertThat(check.getString("status"), is("DOWN"));
                assertThat(check.getJsonObject("data"), is(nullValue()));
            } else {
                Assertions.fail("Health returned unexpected health check: " + check);
            }
        }
    }

    public void testGetHealthWithMixedChecksAndStyleStackTrace(String expectedCheckName, Supplier<SmallRyeHealth> supplier) {
        asyncHealthCheckFactory.setUncheckedExceptionDataStyle("stackTrace");

        SmallRyeHealth health = supplier.get();

        assertThat(health.isDown(), is(true));
        assertThat(health.getPayload().getString("status"), is("DOWN"));

        JsonArray checks = health.getPayload().getJsonArray("checks");
        assertThat(checks.size(), is(3));

        for (JsonObject check : checks.getValuesAs(JsonObject.class)) {
            if (check.getString("name").equals("down")) {
                assertThat(check.getString("status"), is("DOWN"));
            } else if (check.getString("name").equals("up")) {
                assertThat(check.getString("status"), is("UP"));
            } else if (check.getString("name").equals(expectedCheckName)) {
                assertThat(check.getString("status"), is("DOWN"));
                assertThat(check.getJsonObject("data").getString("stackTrace"), is(notNullValue()));
            }
        }
    }

    public void testReport(Runnable prepare, Supplier<SmallRyeHealth> supplier, Supplier<String> expectedContent) {
        ByteArrayOutputStream logStream = new ByteArrayOutputStream();
        Handler handler = new StreamHandler(logStream, new SimpleFormatter());

        Logger logger = Logger.getLogger("io.smallrye.health");
        logger.addHandler(handler);
        logger.setLevel(Level.ALL);
        logger.setUseParentHandlers(false);

        prepare.run();
        reporter.reportHealth(new ByteArrayOutputStream(), supplier.get());

        handler.flush();
        assertThat(logStream.toString(), containsString(expectedContent.get()));
    }
}
