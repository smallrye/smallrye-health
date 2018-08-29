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
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonWriter;
import javax.json.JsonWriterFactory;
import javax.json.stream.JsonGenerator;

import org.eclipse.microprofile.health.Health;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;


@ApplicationScoped
public class SmallRyeHealthReporter {
    private static final Map<String, ?> JSON_CONFIG = Collections.singletonMap(JsonGenerator.PRETTY_PRINTING, true);

    private static String getStackTrace(Throwable t) {
	    StringWriter string = new StringWriter();
	    
	    try (PrintWriter pw = new PrintWriter(string)) {
	        t.printStackTrace(pw);
	    }
	    
	    return string.toString();
	}

	/**
     * can be {@code null} if SmallRyeHealthReporter is used in a non-CDI environment
     */
    @Inject
    @Health
    private Instance<HealthCheck> checks;

    private List<HealthCheck> additionalChecks = new ArrayList<>();

    public void reportHealth(OutputStream out, SmallRyeHealth health) {

        JsonWriterFactory factory = Json.createWriterFactory(JSON_CONFIG);
        JsonWriter writer = factory.createWriter(out);

        writer.writeObject(health.getPayload());
        writer.close();
    }

    public SmallRyeHealth getHealth() {
        JsonArrayBuilder results = Json.createArrayBuilder();
        HealthCheckResponse.State outcome = HealthCheckResponse.State.UP;

        if (checks != null) {
            for (HealthCheck check : checks) {
                outcome = fillCheck(check, results, outcome);
            }
        }
        if (!additionalChecks.isEmpty()) {
            for (HealthCheck check : additionalChecks) {
                outcome = fillCheck(check, results, outcome);
            }
        }

        JsonObjectBuilder builder = Json.createObjectBuilder();


        builder.add("outcome", outcome.toString());
        builder.add("checks", results);

        return new SmallRyeHealth(builder.build());
    }

    private HealthCheckResponse.State fillCheck(HealthCheck check, JsonArrayBuilder results, HealthCheckResponse.State globalOutcome) {
        if (check == null) {
            return globalOutcome;
        }
        JsonObject each = jsonObject(check);
        results.add(each);
        if (globalOutcome == HealthCheckResponse.State.UP) {
            String state = each.getString("state");
            if (state.equals("DOWN")) {
                return HealthCheckResponse.State.DOWN;
            }
        }
        return globalOutcome;
    }
    
    private JsonObject jsonObject(HealthCheck check) {
        try {
            return jsonObject(check.call());
        } catch (RuntimeException e) {
            return jsonObject(HealthCheckResponse.named(check.getClass().getName())
                                                 .withData("stacktrace", getStackTrace(e))
                                                 .down()
                                                 .build());
        }
    }

    private JsonObject jsonObject(HealthCheckResponse response) {
        JsonObjectBuilder builder = Json.createObjectBuilder();
        builder.add("name", response.getName());
        builder.add("state", response.getState().toString());
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
}


