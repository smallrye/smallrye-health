package io.smallrye.health;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Sanity test cases for classes that use injected config values but can be also initialized in non-CDI environments.
 */
public class ProgrammaticAccessSanityTest {

    @Test
    public void testSmallRyeHealthReporterInitialization() {
        SmallRyeHealthReporter reporter = new SmallRyeHealthReporter();
        Assertions.assertNotNull(reporter);
        Assertions.assertNotNull(reporter.emptyChecksOutcome);
        Assertions.assertNotNull(reporter.additionalProperties);
    }

    @Test
    public void testAsyncHealthCheckFactoryInitialization() {
        AsyncHealthCheckFactory factory = new AsyncHealthCheckFactory();
        Assertions.assertNotNull(factory);
        Assertions.assertNotNull(factory.uncheckedExceptionDataStyle);
    }
}
