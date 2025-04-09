package io.smallrye.health.event;

import java.time.Instant;

import io.smallrye.common.annotation.Experimental;
import io.smallrye.health.SmallRyeHealth;
import io.smallrye.health.api.HealthType;

/**
 * Event for the health state changes. Can be consumed with the
 * specification-defined annotations.
 */
@Experimental("Health change events")
public record SmallRyeHealthStatusChangeEvent(Instant timestamp, HealthType healthType, SmallRyeHealth health) {

    public SmallRyeHealthStatusChangeEvent(SmallRyeHealth health) {
        this(null, health);
    }

    public SmallRyeHealthStatusChangeEvent(HealthType healthType, SmallRyeHealth health) {
        this(Instant.now(), healthType, health);
    }
}
