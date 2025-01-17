package io.smallrye.health.api.event;

import java.time.Instant;

import org.eclipse.microprofile.health.HealthCheckResponse;

import io.smallrye.health.api.HealthType;

/**
 * Event interface for the health state changes.
 */
public interface HealthStatusChangeEvent {

    Instant timestamp();

    HealthType healthType();

    HealthCheckResponse.Status status();
}
