package io.smallrye.health;

import java.io.PrintWriter;
import java.io.StringWriter;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.HealthCheckResponseBuilder;

import io.smallrye.health.api.AsyncHealthCheck;
import io.smallrye.mutiny.Uni;

@ApplicationScoped
public class AsyncHealthCheckFactory {

    private static final String ROOT_CAUSE = "rootCause";
    private static final String STACK_TRACE = "stackTrace";

    @Inject
    @ConfigProperty(name = "io.smallrye.health.uncheckedExceptionDataStyle", defaultValue = ROOT_CAUSE)
    String uncheckedExceptionDataStyle;

    public Uni<HealthCheckResponse> callAsync(AsyncHealthCheck asyncHealthCheck) {
        return withRecovery(asyncHealthCheck.getClass().getName(), Uni.createFrom().deferred(asyncHealthCheck::call));
    }

    public Uni<HealthCheckResponse> callSync(HealthCheck healthCheck) {
        return withRecovery(healthCheck.getClass().getName(), Uni.createFrom().item(healthCheck::call));
    }

    private Uni<HealthCheckResponse> withRecovery(String name, Uni<HealthCheckResponse> uni) {
        return uni.onFailure().recoverWithItem(e -> handleFailure(name, e))
                .onItem().ifNull()
                .continueWith(() -> handleFailure(name, HealthMessages.msg.healthCheckNull()));
    }

    private HealthCheckResponse handleFailure(String name, Throwable e) {
        // Log Stacktrace to server log so an error is not just in Health Check response
        HealthLogging.log.healthCheckError(e);

        HealthCheckResponseBuilder response = HealthCheckResponse.named(name).down();

        if (null != uncheckedExceptionDataStyle) {
            switch (uncheckedExceptionDataStyle) {
                case ROOT_CAUSE:
                    response.withData(ROOT_CAUSE, getRootCause(e).getMessage());
                    break;
                case STACK_TRACE:
                    response.withData(STACK_TRACE, getStackTrace(e));
                    break;
                default:
                    // don't add anything
            }
        }

        return response.build();
    }

    private static String getStackTrace(Throwable t) {
        StringWriter string = new StringWriter();

        try (PrintWriter pw = new PrintWriter(string)) {
            t.printStackTrace(pw);
        }

        return string.toString();
    }

    private static Throwable getRootCause(Throwable t) {
        Throwable cause = t.getCause();

        if (cause == null || cause == t) {
            return t;
        }

        return getRootCause(cause);
    }

    public void setUncheckedExceptionDataStyle(String uncheckedExceptionDataStyle) {
        this.uncheckedExceptionDataStyle = uncheckedExceptionDataStyle;
    }
}
