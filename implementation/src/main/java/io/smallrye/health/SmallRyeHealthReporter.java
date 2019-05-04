package io.smallrye.health;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonWriter;
import javax.json.JsonWriterFactory;
import javax.json.stream.JsonGenerator;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.health.Health;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.HealthCheckResponseBuilder;
import org.jboss.logging.Logger;


@ApplicationScoped
public class SmallRyeHealthReporter {
    private static Logger LOG = Logger.getLogger(SmallRyeHealthReporter.class);

    private static final String ROOT_CAUSE = "rootCause";

    private static final String STACK_TRACE = "stackTrace";

    private static final Map<String, ?> JSON_CONFIG = Collections.singletonMap(JsonGenerator.PRETTY_PRINTING, true);

    /**
     * can be {@code null} if SmallRyeHealthReporter is used in a non-CDI environment
     */
    @Inject
    @Health
    Instance<HealthCheck> checks;

    @Inject
    @ConfigProperty(name = "io.smallrye.health.uncheckedExceptionDataStyle", defaultValue = ROOT_CAUSE)
    String uncheckedExceptionDataStyle;

    @Inject
    @ConfigProperty(name = "io.smallrye.health.emptyChecksOutcome", defaultValue = "UP")
    String emptyChecksOutcome;

    private List<HealthCheck> additionalChecks = new ArrayList<>();

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

        JsonWriterFactory factory = Json.createWriterFactory(JSON_CONFIG);
        JsonWriter writer = factory.createWriter(out);

        writer.writeObject(health.getPayload());
        writer.close();
    }

    public SmallRyeHealth getHealth() {
        JsonArrayBuilder results = Json.createArrayBuilder();
        HealthCheckResponse.State status = HealthCheckResponse.State.UP;

        if (checks != null) {
            for (HealthCheck check : checks) {
                status = fillCheck(check, results, status);
            }
        }
        if (!additionalChecks.isEmpty()) {
            for (HealthCheck check : additionalChecks) {
                status = fillCheck(check, results, status);
            }
        }

        JsonObjectBuilder builder = Json.createObjectBuilder();

        JsonArray checkResults = results.build();

        builder.add("status", checkResults.isEmpty() ? emptyChecksOutcome : status.toString());
        builder.add("checks", checkResults);

        return new SmallRyeHealth(builder.build());
    }

    private HealthCheckResponse.State fillCheck(HealthCheck check, JsonArrayBuilder results, HealthCheckResponse.State globalOutcome) {
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
            LOG.error("Error processing Health Checks", e);

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
        JsonObjectBuilder builder = Json.createObjectBuilder();
        builder.add("name", response.getName());
        builder.add("status", response.getState().toString());
        response.getData().ifPresent(d -> {
            JsonObjectBuilder data = Json.createObjectBuilder();
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


