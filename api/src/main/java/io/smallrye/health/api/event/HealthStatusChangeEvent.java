package io.smallrye.health.api.event;

import java.time.Instant;

import org.eclipse.microprofile.health.HealthCheckResponse;

import io.smallrye.common.annotation.Experimental;
import io.smallrye.health.api.HealthType;

/**
 * Event for the health state changes. Can be consumed with the
 * specification-defined annotations.
 */
@Experimental("Health change events")
public record HealthStatusChangeEvent(Instant timestamp, HealthType healthType, HealthCheckResponse.Status status) {

    public HealthStatusChangeEvent(HealthCheckResponse.Status status) {
        this(null, status);
    }

    public HealthStatusChangeEvent(HealthType healthType, HealthCheckResponse.Status status) {
        this(Instant.now(), healthType, status);
    }
}
