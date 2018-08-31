package io.smallrye.health;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.junit.Assert.assertThat;

import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.junit.Test;

import io.smallrye.health.SmallRyeHealthReporter.UncheckedExceptionDataStyle;

public class SmallRyeHealthReporterTest {
    public static class FailingHealthCheck
    implements HealthCheck {
        @Override
        public HealthCheckResponse call() {
            throw new RuntimeException("this health check has failed");
        }
    }
	
    public static class DownHealthCheck
    implements HealthCheck {
        @Override
        public HealthCheckResponse call() {
            return HealthCheckResponse.named("down").down().build();
        }
    }
	
    public static class UpHealthCheck
    implements HealthCheck {
        @Override
        public HealthCheckResponse call() {
            return HealthCheckResponse.named("up").up().build();
        }
    }
	
	@Test
    public void testDefaultGetHealth() {
        SmallRyeHealthReporter reporter = new SmallRyeHealthReporter();
        
        SmallRyeHealth health = reporter.getHealth();
        
        assertThat(health.isDown(), is(false));
        assertThat(health.getPayload().getString("outcome"), is("UP"));
        assertThat(health.getPayload().getJsonArray("checks"), is(empty()));
    }
	
	@Test
    public void testGetHealthWithFailingCheckAndStyleDefault() {
        SmallRyeHealthReporter reporter = new SmallRyeHealthReporter();
        
        reporter.addHealthCheck(new FailingHealthCheck());
        
        SmallRyeHealth health = reporter.getHealth();
        
        assertThat(health.isDown(), is(true));
        assertThat(health.getPayload().getString("outcome"), is("DOWN"));
        assertThat(health.getPayload().getJsonArray("checks").getJsonObject(0).getString("name"), is(FailingHealthCheck.class.getName()));
        assertThat(health.getPayload().getJsonArray("checks").getJsonObject(0).getString("state"), is("DOWN"));
        assertThat(health.getPayload().getJsonArray("checks").getJsonObject(0).getJsonObject("data").getString("rootCause"), is("this health check has failed"));
    }
	
	@Test
    public void testGetHealthWithFailingCheckAndStyleNone() {
        SmallRyeHealthReporter reporter = new SmallRyeHealthReporter();
        
        reporter.addHealthCheck(new FailingHealthCheck());
        reporter.setUncheckedExceptionDataStyle(UncheckedExceptionDataStyle.NONE);
        
        SmallRyeHealth health = reporter.getHealth();
        
        assertThat(health.isDown(), is(true));
        assertThat(health.getPayload().getString("outcome"), is("DOWN"));
        assertThat(health.getPayload().getJsonArray("checks").getJsonObject(0).getString("name"), is(FailingHealthCheck.class.getName()));
        assertThat(health.getPayload().getJsonArray("checks").getJsonObject(0).getString("state"), is("DOWN"));
        assertThat(health.getPayload().getJsonArray("checks").getJsonObject(0).getJsonObject("data"), is(nullValue()));
    }
	
	@Test
    public void testGetHealthWithFailingCheckAndStyleStackTrace() {
        SmallRyeHealthReporter reporter = new SmallRyeHealthReporter();
        
        reporter.addHealthCheck(new FailingHealthCheck());
        reporter.setUncheckedExceptionDataStyle(UncheckedExceptionDataStyle.STACK_TRACE);
        
        SmallRyeHealth health = reporter.getHealth();
        
        assertThat(health.isDown(), is(true));
        assertThat(health.getPayload().getString("outcome"), is("DOWN"));
        assertThat(health.getPayload().getJsonArray("checks").getJsonObject(0).getString("name"), is(FailingHealthCheck.class.getName()));
        assertThat(health.getPayload().getJsonArray("checks").getJsonObject(0).getString("state"), is("DOWN"));
        assertThat(health.getPayload().getJsonArray("checks").getJsonObject(0).getJsonObject("data").getString("stackTrace"), is(notNullValue()));
    }

	@Test
    public void testGetHealthWithMixedChecksAndStyleDefault() {
        SmallRyeHealthReporter reporter = new SmallRyeHealthReporter();
        
        reporter.addHealthCheck(new UpHealthCheck());
        reporter.addHealthCheck(new FailingHealthCheck());
        reporter.addHealthCheck(new DownHealthCheck());
        
        SmallRyeHealth health = reporter.getHealth();
        
        assertThat(health.isDown(), is(true));
        assertThat(health.getPayload().getString("outcome"), is("DOWN"));
        assertThat(health.getPayload().getJsonArray("checks").getJsonObject(0).getString("name"), is("up"));
        assertThat(health.getPayload().getJsonArray("checks").getJsonObject(0).getString("state"), is("UP"));
        assertThat(health.getPayload().getJsonArray("checks").getJsonObject(1).getString("name"), is(FailingHealthCheck.class.getName()));
        assertThat(health.getPayload().getJsonArray("checks").getJsonObject(1).getString("state"), is("DOWN"));
        assertThat(health.getPayload().getJsonArray("checks").getJsonObject(1).getJsonObject("data").getString("rootCause"), is("this health check has failed"));
        assertThat(health.getPayload().getJsonArray("checks").getJsonObject(2).getString("name"), is("down"));
        assertThat(health.getPayload().getJsonArray("checks").getJsonObject(2).getString("state"), is("DOWN"));
    }
	
	@Test
    public void testGetHealthWithMixedChecksAndStyleNone() {
        SmallRyeHealthReporter reporter = new SmallRyeHealthReporter();
        
        reporter.addHealthCheck(new UpHealthCheck());
        reporter.addHealthCheck(new FailingHealthCheck());
        reporter.addHealthCheck(new DownHealthCheck());
        reporter.setUncheckedExceptionDataStyle(UncheckedExceptionDataStyle.NONE);
        
        SmallRyeHealth health = reporter.getHealth();
        
        assertThat(health.isDown(), is(true));
        assertThat(health.getPayload().getString("outcome"), is("DOWN"));
        assertThat(health.getPayload().getJsonArray("checks").getJsonObject(0).getString("name"), is("up"));
        assertThat(health.getPayload().getJsonArray("checks").getJsonObject(0).getString("state"), is("UP"));
        assertThat(health.getPayload().getJsonArray("checks").getJsonObject(1).getString("name"), is(FailingHealthCheck.class.getName()));
        assertThat(health.getPayload().getJsonArray("checks").getJsonObject(1).getString("state"), is("DOWN"));
        assertThat(health.getPayload().getJsonArray("checks").getJsonObject(1).getJsonObject("data"), is(nullValue()));
        assertThat(health.getPayload().getJsonArray("checks").getJsonObject(2).getString("name"), is("down"));
        assertThat(health.getPayload().getJsonArray("checks").getJsonObject(2).getString("state"), is("DOWN"));
    }
	
	@Test
    public void testGetHealthWithMixedChecksAndStyleStackTrace() {
        SmallRyeHealthReporter reporter = new SmallRyeHealthReporter();
        
        reporter.addHealthCheck(new UpHealthCheck());
        reporter.addHealthCheck(new FailingHealthCheck());
        reporter.addHealthCheck(new DownHealthCheck());
        reporter.setUncheckedExceptionDataStyle(UncheckedExceptionDataStyle.STACK_TRACE);
        
        SmallRyeHealth health = reporter.getHealth();
        
        assertThat(health.isDown(), is(true));
        assertThat(health.getPayload().getString("outcome"), is("DOWN"));
        assertThat(health.getPayload().getJsonArray("checks").getJsonObject(0).getString("name"), is("up"));
        assertThat(health.getPayload().getJsonArray("checks").getJsonObject(0).getString("state"), is("UP"));
        assertThat(health.getPayload().getJsonArray("checks").getJsonObject(1).getString("name"), is(FailingHealthCheck.class.getName()));
        assertThat(health.getPayload().getJsonArray("checks").getJsonObject(1).getString("state"), is("DOWN"));
        assertThat(health.getPayload().getJsonArray("checks").getJsonObject(1).getJsonObject("data").getString("stackTrace"), is(notNullValue()));
        assertThat(health.getPayload().getJsonArray("checks").getJsonObject(2).getString("name"), is("down"));
        assertThat(health.getPayload().getJsonArray("checks").getJsonObject(2).getString("state"), is("DOWN"));
    }
}
