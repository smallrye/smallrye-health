package io.smallrye.health;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import jakarta.enterprise.context.ApplicationScoped;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.HealthCheckResponseBuilder;
import org.jboss.logging.Logger;

import io.smallrye.health.api.AsyncHealthCheck;
import io.smallrye.health.log.ExceptionErrorIdDetail;
import io.smallrye.health.log.ExceptionLogType;
import io.smallrye.mutiny.Uni;

@ApplicationScoped
public class AsyncHealthCheckFactory {

    private static final String EXCEPTION_CLASS = "exceptionClass";
    private static final String EXCEPTION_MESSAGE = "exceptionMessage";
    private static final String ROOT_CAUSE = "rootCause";
    private static final String STACK_TRACE = "stackTrace";

    // TODO unify these properties in the next major release
    String uncheckedExceptionDataStyle = ROOT_CAUSE;
    ExceptionLogType exceptionLogType = ExceptionLogType.NONE;
    ExceptionErrorIdDetail exceptionErrorIdDetail = ExceptionErrorIdDetail.STACKTRACE;
    Logger.Level exceptionLogLevel = Logger.Level.ERROR;
    boolean removeDeprecatedExceptionDataStyle = false;

    public AsyncHealthCheckFactory() {
        try {
            Config config = ConfigProvider.getConfig();
            Optional<String> optionalUncheckedExceptionDataStyle = config
                    .getOptionalValue("io.smallrye.health.uncheckedExceptionDataStyle", String.class);
            if (optionalUncheckedExceptionDataStyle.isPresent()) {
                HealthLogging.logger
                        .warn("Configuration property \"io.smallrye.health.uncheckedExceptionDataStyle\" is deprecated. Use " +
                                "\"io.smallrye.health.exception-log-type\" and \"io.smallrye.health.exception.errorid.detail\" instead.");
            }
            uncheckedExceptionDataStyle = optionalUncheckedExceptionDataStyle.orElse(ROOT_CAUSE);

            Optional<ExceptionLogType> optionalExceptionLogType = config
                    .getOptionalValue("io.smallrye.health.exception.log.type", ExceptionLogType.class);
            if (optionalExceptionLogType.isPresent()) {
                removeDeprecatedExceptionDataStyle = true;
            }
            exceptionLogType = optionalExceptionLogType.orElse(ExceptionLogType.NONE);
            exceptionErrorIdDetail = config
                    .getOptionalValue("io.smallrye.health.exception.errorid.detail", ExceptionErrorIdDetail.class)
                    .orElse(ExceptionErrorIdDetail.STACKTRACE);
            exceptionLogLevel = config.getOptionalValue("io.smallrye.health.exception.log.level", Logger.Level.class)
                    .orElse(Logger.Level.ERROR);
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

        String errorKey = "<error>";
        switch (exceptionLogType) {
            case NONE:
                break;
            case ERROR_ID:
                UUID uuid = UUID.randomUUID();
                response.withData(errorKey, "See error-id " + uuid);
                String errorToLog = switch (exceptionErrorIdDetail) {
                    case STACKTRACE -> getStackTrace(e);
                    case EXCEPTION_CLASS -> e.getClass().getName();
                    case EXCEPTION_MESSAGE -> getRootCause(e).toString();
                };
                HealthLogging.logger.log(exceptionLogLevel,
                        "Health check \"%s\" failed (error-id %s) %s".formatted(name, uuid, errorToLog));
                break;
            case EXCEPTION_CLASS:
                response.withData(errorKey, e.getClass().getName());
                break;
            case EXCEPTION_MESSAGE:
                response.withData(errorKey, getRootCause(e).toString());
        }

        if (!removeDeprecatedExceptionDataStyle && !uncheckedExceptionDataStyle.equals("none")) {
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
