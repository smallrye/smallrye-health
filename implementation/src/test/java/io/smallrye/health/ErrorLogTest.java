package io.smallrye.health;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.util.logging.Level;

import jakarta.json.JsonObject;

import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.jboss.logging.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.smallrye.health.log.ExceptionErrorIdDetail;
import io.smallrye.health.log.ExceptionLogType;
import io.smallrye.testing.logging.LogCapture;

/**
 * The new log API test. Will replace the log tests in {@link SmallRyeHealthReporterTest}.
 */
public class ErrorLogTest {

    @RegisterExtension
    static LogCapture logCapture = LogCapture.with(logRecord -> true, Level.ALL);

    public static class FailingHealthCheck implements HealthCheck {
        @Override
        public HealthCheckResponse call() {
            throw new RuntimeException("this health check has failed");
        }
    }

    @BeforeEach
    public void createReporter() {
        logCapture.records().clear();
    }

    @Test
    public void testReportWhenDownExceptionTypeNone() {
        SmallRyeHealth health = reportHealthWithFailure(ExceptionLogType.NONE);

        assertNull(health.getPayload().getJsonArray("checks").getJsonObject(0).getJsonObject("data"));
        assertAnyLogMessageContains("SRHCK01001: Reporting health down status: {\"status\":\"DOWN\","
                + "\"checks\":[{\"name\":\"io.smallrye.health.ErrorLogTest$FailingHealthCheck\",\"status\":\"DOWN\"}]}");
    }

    @Test
    public void testReportWhenDownExceptionTypeErrorIdStacktrace() {
        SmallRyeHealth health = reportHealthWithFailure(ExceptionLogType.ERROR_ID);

        JsonObject data = health.getPayload().getJsonArray("checks").getJsonObject(0).getJsonObject("data");
        assertNotNull(data);
        assertEquals(1, data.size());
        assertTrue(data.getString("<error>").contains("See error-id "));
        assertAnyLogMessageContains("SRHCK01001: Reporting health down status: {\"status\":\"DOWN\"");
        assertAnyLogMessageMatches("Health check \"io\\.smallrye\\.health\\.ErrorLogTest\\$FailingHealthCheck\" failed "
                + "\\(error-id [0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}\\) "
                + "java\\.lang\\.RuntimeException: this health check has failed\\R"
                + "\\s*at io\\.smallrye\\.health\\.ErrorLogTest\\$FailingHealthCheck\\.call[\\s\\S]*", Logger.Level.ERROR);
    }

    @Test
    public void testReportWhenDownExceptionTypeErrorIdExceptionClass() {
        SmallRyeHealth health = reportHealthWithFailure(ExceptionLogType.ERROR_ID, ExceptionErrorIdDetail.EXCEPTION_CLASS);

        JsonObject data = health.getPayload().getJsonArray("checks").getJsonObject(0).getJsonObject("data");
        assertNotNull(data);
        assertEquals(1, data.size());
        assertTrue(data.getString("<error>").contains("See error-id "));
        assertAnyLogMessageContains("SRHCK01001: Reporting health down status: {\"status\":\"DOWN\"");
        assertAnyLogMessageMatches("Health check \"io\\.smallrye\\.health\\.ErrorLogTest\\$FailingHealthCheck\" failed " +
                "\\(error-id [0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}\\) " +
                "java\\.lang\\.RuntimeException$", Logger.Level.ERROR);
    }

    @Test
    public void testReportWhenDownExceptionTypeErrorIdExceptionMessage() {
        SmallRyeHealth health = reportHealthWithFailure(ExceptionLogType.ERROR_ID, ExceptionErrorIdDetail.EXCEPTION_MESSAGE);

        JsonObject data = health.getPayload().getJsonArray("checks").getJsonObject(0).getJsonObject("data");
        assertNotNull(data);
        assertEquals(1, data.size());
        assertTrue(data.getString("<error>").contains("See error-id "));
        assertAnyLogMessageContains("SRHCK01001: Reporting health down status: {\"status\":\"DOWN\"");
        assertAnyLogMessageMatches("Health check \"io\\.smallrye\\.health\\.ErrorLogTest\\$FailingHealthCheck\" failed " +
                "\\(error-id [0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}\\) " +
                "java\\.lang\\.RuntimeException: this health check has failed$", Logger.Level.ERROR);
    }

    @Test
    public void testReportWhenDownExceptionTypeExceptionClass() {
        SmallRyeHealth health = reportHealthWithFailure(ExceptionLogType.EXCEPTION_CLASS);

        JsonObject data = health.getPayload().getJsonArray("checks").getJsonObject(0).getJsonObject("data");
        assertNotNull(data);
        assertEquals(1, data.size());
        assertTrue(data.getString("<error>").contains("java.lang.RuntimeException"));
        assertAnyLogMessageContains("SRHCK01001: Reporting health down status: {\"status\":\"DOWN\"");
    }

    @Test
    public void testReportWhenDownExceptionTypeExceptionMessage() {
        SmallRyeHealth health = reportHealthWithFailure(ExceptionLogType.EXCEPTION_MESSAGE);

        JsonObject data = health.getPayload().getJsonArray("checks").getJsonObject(0).getJsonObject("data");
        assertNotNull(data);
        assertEquals(1, data.size());
        assertTrue(data.getString("<error>").contains("java.lang.RuntimeException: this health check has failed"));
        assertAnyLogMessageContains("SRHCK01001: Reporting health down status: {\"status\":\"DOWN\"");
    }

    @Test
    public void testReportWhenDownExceptionLogLevelWithErrorId() {
        AsyncHealthCheckFactory asyncHealthCheckFactory = new AsyncHealthCheckFactory();
        asyncHealthCheckFactory.exceptionLogType = ExceptionLogType.ERROR_ID;
        asyncHealthCheckFactory.exceptionLogLevel = Logger.Level.DEBUG;
        asyncHealthCheckFactory.removeDeprecatedExceptionDataStyle = true;
        SmallRyeHealthReporter reporter = new SmallRyeHealthReporter();
        reporter.asyncHealthCheckFactory = asyncHealthCheckFactory;
        reporter.addHealthCheck(new FailingHealthCheck());
        SmallRyeHealth health = reporter.getHealth();
        reporter.reportHealth(new ByteArrayOutputStream(), health);

        JsonObject data = health.getPayload().getJsonArray("checks").getJsonObject(0).getJsonObject("data");
        assertNotNull(data);
        assertEquals(1, data.size());
        assertTrue(data.getString("<error>").contains("See error-id "));
        assertAnyLogMessageContains("SRHCK01001: Reporting health down status: {\"status\":\"DOWN\"");
        assertAnyLogMessageMatches("Health check \"io\\.smallrye\\.health\\.ErrorLogTest\\$FailingHealthCheck\" failed "
                + "\\(error-id [0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}\\) "
                + "java\\.lang\\.RuntimeException: this health check has failed\\R"
                + "\\s*at io\\.smallrye\\.health\\.ErrorLogTest\\$FailingHealthCheck\\.call[\\s\\S]*", Logger.Level.DEBUG);
    }

    private SmallRyeHealth reportHealthWithFailure(ExceptionLogType type) {
        return reportHealthWithFailure(type, ExceptionErrorIdDetail.STACKTRACE);
    }

    private SmallRyeHealth reportHealthWithFailure(ExceptionLogType exceptionLogType,
            ExceptionErrorIdDetail exceptionErrorIdDetail) {
        SmallRyeHealthReporter reporter = createReporter(exceptionLogType, exceptionErrorIdDetail);
        reporter.addHealthCheck(new FailingHealthCheck());
        SmallRyeHealth health = reporter.getHealth();
        reporter.reportHealth(new ByteArrayOutputStream(), health);
        return health;
    }

    private static SmallRyeHealthReporter createReporter(ExceptionLogType exceptionLogType,
            ExceptionErrorIdDetail exceptionErrorIdDetail) {
        AsyncHealthCheckFactory asyncHealthCheckFactory = new AsyncHealthCheckFactory();
        asyncHealthCheckFactory.removeDeprecatedExceptionDataStyle = true;
        asyncHealthCheckFactory.exceptionLogType = exceptionLogType;
        asyncHealthCheckFactory.exceptionErrorIdDetail = exceptionErrorIdDetail;
        SmallRyeHealthReporter reporter = new SmallRyeHealthReporter();
        reporter.asyncHealthCheckFactory = asyncHealthCheckFactory;
        return reporter;
    }

    private void assertAnyLogMessageContains(String expected) {
        assertFalse(logCapture.records().isEmpty(), "Expected that the log is not empty");
        assertTrue(logCapture.records().stream().anyMatch(logRecord -> logRecord.getMessage().contains(expected)),
                "Log doesn't contain requested message: " + expected);
    }

    private void assertAnyLogMessageMatches(String regex, Logger.Level level) {
        assertFalse(logCapture.records().isEmpty(), "Expected that the log is not empty");
        assertTrue(
                logCapture.records().stream()
                        .anyMatch(logRecord -> logRecord.getMessage().matches(regex)
                                && (level == null || level.equals(Logger.Level.valueOf(level.toString())))),
                "Log doesn't contain requested message matching: " + regex);
    }
}
