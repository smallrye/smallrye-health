package io.smallrye.health;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Objects;

import jakarta.enterprise.context.ApplicationScoped;

import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.HealthCheckResponseBuilder;

import io.smallrye.health.api.AsyncHealthCheck;
import io.smallrye.mutiny.Uni;

@ApplicationScoped
public class AsyncHealthCheckFactory {

    private static final String EXCEPTION_CLASS = "exceptionClass";
    private static final String EXCEPTION_MESSAGE = "exceptionMessage";
    private static final String ROOT_CAUSE = "rootCause";
    private static final String STACK_TRACE = "stackTrace";

    String uncheckedExceptionDataStyle = ROOT_CAUSE;

    public AsyncHealthCheckFactory() {
        try {
            uncheckedExceptionDataStyle = ConfigProvider.getConfig()
                    .getOptionalValue("io.smallrye.health.uncheckedExceptionDataStyle", String.class)
                    .orElse(ROOT_CAUSE);
        } catch (IllegalStateException illegalStateException) {
            // OK, no config provider was found, use default values
        }
    }

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
        HealthLogging.logger.healthCheckError(e);

        HealthCheckResponseBuilder response = HealthCheckResponse.named(name).down();

        if (!uncheckedExceptionDataStyle.equals("none")) {
            response.withData(EXCEPTION_CLASS, e.getClass().getName());
            response.withData(EXCEPTION_MESSAGE, e.getMessage());

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

    // Manual config overrides

    public void setUncheckedExceptionDataStyle(String uncheckedExceptionDataStyle) {
        Objects.requireNonNull(uncheckedExceptionDataStyle);
        this.uncheckedExceptionDataStyle = uncheckedExceptionDataStyle;
    }
}
