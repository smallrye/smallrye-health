package io.smallrye.health.registry;

import jakarta.enterprise.context.ApplicationScoped;

import io.smallrye.health.api.HealthRegistry;
import io.smallrye.health.api.Wellness;

@Wellness
@ApplicationScoped
public class WellnessHealthRegistry extends AbstractHealthRegistry implements HealthRegistry {
}
