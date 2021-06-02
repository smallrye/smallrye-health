package io.smallrye.health.registry;

import javax.enterprise.context.ApplicationScoped;

import io.smallrye.health.api.HealthRegistry;
import io.smallrye.health.api.Wellness;

@Wellness
@ApplicationScoped
public class WellnessHealthRegistry extends AbstractHealthRegistry implements HealthRegistry {
}
