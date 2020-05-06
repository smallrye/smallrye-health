package io.smallrye.health;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonWriter;
import javax.json.JsonWriterFactory;
import javax.json.spi.JsonProvider;
import javax.json.stream.JsonGenerator;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.health.Health;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.HealthCheckResponseBuilder;
import org.eclipse.microprofile.health.Liveness;
import org.eclipse.microprofile.health.Readiness;

@ApplicationScoped
public class SmallRyeHealthReporter {
    private static final String ROOT_CAUSE = "rootCause";

    private static final String STACK_TRACE = "stackTrace";

    private static final Map<String, ?> JSON_CONFIG = Collections.singletonMap(JsonGenerator.PRETTY_PRINTING, true);

    /**
     * can be {@code null} if SmallRyeHealthReporter is used in a non-CDI environment
     */
    @Inject
    @Health
    Instance<HealthCheck> healthChecks;

    @Inject
    @Liveness
    Instance<HealthCheck> livenessChecks;

    @Inject
    @Readiness
    Instance<HealthCheck> readinessChecks;

    @Inject
    @Any
    Instance<HealthCheck> allHealthChecks;

    @Inject
    @ConfigProperty(name = "io.smallrye.health.uncheckedExceptionDataStyle", defaultValue = ROOT_CAUSE)
    String uncheckedExceptionDataStyle;

    @Inject
    @ConfigProperty(name = "io.smallrye.health.emptyChecksOutcome", defaultValue = "UP")
    String emptyChecksOutcome;

    private List<HealthCheck> additionalChecks = new ArrayList<>();

    private JsonProvider jsonProvider = JsonProvider.provider();

    void setUncheckedExceptionDataStyle(String uncheckedExceptionDataStyle) {
        if (uncheckedExceptionDataStyle != null) {
            this.uncheckedExceptionDataStyle = uncheckedExceptionDataStyle;
        }
    }

    void setEmptyChecksOutcome(String emptyChecksOutcome) {
        if (emptyChecksOutcome != null) {
            this.emptyChecksOutcome = emptyChecksOutcome;
        }
    }

    public void reportHealth(OutputStream out, SmallRyeHealth health) {
        if (health.isDown() && HealthLogging.log.isInfoEnabled()) {
            // Log reason, as not reported by container orchestrators, yet container may get killed.
            HealthLogging.log.healthDownStatus(health.getPayload().toString());
        }

        JsonWriterFactory factory = jsonProvider.createWriterFactory(JSON_CONFIG);
        JsonWriter writer = factory.createWriter(out);

        writer.writeObject(health.getPayload());
        writer.close();
    }

    public SmallRyeHealth getHealth() {
        return getHealth(healthChecks, livenessChecks, readinessChecks);
    }

    public SmallRyeHealth getLiveness() {
        return getHealth(livenessChecks);
    }

    public SmallRyeHealth getReadiness() {
        return getHealth(readinessChecks);
    }

    public SmallRyeHealth getHealthGroup(String groupName) {
        return getHealth(allHealthChecks.select(HealthGroup.Literal.of(groupName)));
    }

    @Inject
    BeanManager beanManager;

    public SmallRyeHealth getHealthGroups() {
        Iterator<Bean<?>> iterator = beanManager.getBeans(HealthCheck.class, Any.Literal.INSTANCE).iterator();

        List<HealthCheck> groupHealthChecks = new ArrayList<>();

        while (iterator.hasNext()) {
            Bean<?> bean = iterator.next();
            if (bean.getQualifiers().stream().anyMatch(annotation -> annotation.annotationType().equals(HealthGroup.class))) {
                groupHealthChecks.add((HealthCheck) beanManager.getReference(bean, bean.getBeanClass(),
                        beanManager.createCreationalContext(bean)));
            }
        }

        return getHealth(groupHealthChecks);
    }

    @SafeVarargs
    private final SmallRyeHealth getHealth(Iterable<HealthCheck>... checks) {
        JsonArrayBuilder results = jsonProvider.createArrayBuilder();
        HealthCheckResponse.State status = HealthCheckResponse.State.UP;

        if (checks != null) {
            for (Iterable<HealthCheck> instance : checks) {
                status = processChecks(instance, results, status);
            }
        }

        if (!additionalChecks.isEmpty()) {
            status = processChecks(additionalChecks, results, status);
        }

        JsonObjectBuilder builder = jsonProvider.createObjectBuilder();

        JsonArray checkResults = results.build();

        builder.add("status", checkResults.isEmpty() ? emptyChecksOutcome : status.toString());
        builder.add("checks", checkResults);

        return new SmallRyeHealth(builder.build());
    }

    private HealthCheckResponse.State processChecks(Iterable<HealthCheck> checks, JsonArrayBuilder results,
            HealthCheckResponse.State status) {
        if (checks != null) {
            for (HealthCheck check : checks) {
                status = fillCheck(check, results, status);
            }
        }

        return status;
    }

    private HealthCheckResponse.State fillCheck(HealthCheck check, JsonArrayBuilder results,
            HealthCheckResponse.State globalOutcome) {
        if (check == null) {
            return globalOutcome;
        }
        JsonObject each = jsonObject(check);
        results.add(each);
        if (globalOutcome == HealthCheckResponse.State.UP) {
            String status = each.getString("status");
            if (status.equals("DOWN")) {
                return HealthCheckResponse.State.DOWN;
            }
        }
        return globalOutcome;
    }

    private JsonObject jsonObject(HealthCheck check) {
        try {
            return jsonObject(check.call());
        } catch (RuntimeException e) {
            // Log Stacktrace to server log so an error is not just in Health Check response
            HealthLogging.log.healthCheckError(e);

            HealthCheckResponseBuilder response = HealthCheckResponse.named(check.getClass().getName()).down();

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

            return jsonObject(response.build());
        }
    }

    private JsonObject jsonObject(HealthCheckResponse response) {
        JsonObjectBuilder builder = jsonProvider.createObjectBuilder();
        builder.add("name", response.getName());
        builder.add("status", response.getState().toString());
        response.getData().ifPresent(d -> {
            JsonObjectBuilder data = jsonProvider.createObjectBuilder();
            for (Map.Entry<String, Object> entry : d.entrySet()) {
                Object value = entry.getValue();
                if (value instanceof String) {
                    data.add(entry.getKey(), (String) value);
                } else if (value instanceof Long) {
                    data.add(entry.getKey(), (Long) value);
                } else if (value instanceof Boolean) {
                    data.add(entry.getKey(), (Boolean) value);
                }
            }
            builder.add("data", data.build());
        });

        return builder.build();
    }

    public void addHealthCheck(HealthCheck check) {
        if (check != null) {
            additionalChecks.add(check);
        }
    }

    public void removeHealthCheck(HealthCheck check) {
        additionalChecks.remove(check);
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

}
