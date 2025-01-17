package io.smallrye.health.event;

import java.time.Instant;

import org.eclipse.microprofile.health.HealthCheckResponse;

import io.smallrye.health.api.HealthType;
import io.smallrye.health.api.event.HealthStatusChangeEvent;

public class SmallRyeHealthStatusChangeEvent implements HealthStatusChangeEvent {

    private final Instant timestamp;
    private final HealthType healthType;
    private final HealthCheckResponse.Status status;

    public SmallRyeHealthStatusChangeEvent(HealthType healthType, HealthCheckResponse.Status status) {
        this.timestamp = Instant.now();
        this.healthType = healthType;
        this.status = status;
    }

    @Override
    public Instant timestamp() {
        return timestamp;
    }

    @Override
    public HealthType healthType() {
        return healthType;
    }

    @Override
    public HealthCheckResponse.Status status() {
        return status;
    }
}
