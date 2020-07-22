package io.smallrye.health;

import java.util.Objects;

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
        return HealthCheckResponse.Status.DOWN.toString().equals(payload.getString("status"));
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getPayload());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof SmallRyeHealth)) {
            return false;
        }
        SmallRyeHealth other = (SmallRyeHealth) obj;
        return Objects.equals(this.getPayload(), other.getPayload());
    }
}
