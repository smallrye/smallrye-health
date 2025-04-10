package io.smallrye.health.event;

import java.time.Instant;

import io.smallrye.common.annotation.Experimental;
import io.smallrye.health.SmallRyeHealth;
import io.smallrye.health.api.HealthType;

/**
 * Event for the health state changes. Can be consumed as a CDI event utilizing the
 * specification-defined qualifiers ({@link org.eclipse.microprofile.health.Liveness},
 * {@link org.eclipse.microprofile.health.Readiness}).
 * <br/>
 * Example:
 *
 * <pre>
 * public void observeLiveness(@Observes @Liveness SmallRyeHealthStatusChangeEvent event) {
 *   ...
 * }
 * </pre>
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
