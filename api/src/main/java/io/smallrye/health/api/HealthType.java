package io.smallrye.health.api;

/**
 * The default types of health representing out-of-the-box health check groups.
 */
public enum HealthType {
    LIVENESS,
    READINESS,
    WELLNESS,
    STARTUP
}
