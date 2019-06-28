package io.smallrye.health;

import javax.json.JsonObject;

import org.eclipse.microprofile.health.HealthCheckResponse;

public class SmallRyeHealth {

    private JsonObject payload;

    public SmallRyeHealth(JsonObject payload) {
        this.payload = payload;
    }

    public JsonObject getPayload() {
        return payload;
    }

    public boolean isDown() {
        return HealthCheckResponse.State.DOWN.toString().equals(payload.getString("status"));
    }
}
