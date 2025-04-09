package io.smallrye.health;

import static org.eclipse.microprofile.health.HealthCheckResponse.Status.DOWN;

import java.util.Objects;

import jakarta.json.JsonObject;

import org.eclipse.microprofile.health.HealthCheckResponse;

public class SmallRyeHealth {

    private final JsonObject payload;
    private final HealthCheckResponse.Status status;

    public SmallRyeHealth(JsonObject payload) {
        this.payload = payload;
        this.status = HealthCheckResponse.Status.valueOf(payload.getString("status"));
    }

    public JsonObject getPayload() {
        return payload;
    }

    public HealthCheckResponse.Status getStatus() {
        return status;
    }

    public boolean isDown() {
        return status.equals(DOWN);
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
