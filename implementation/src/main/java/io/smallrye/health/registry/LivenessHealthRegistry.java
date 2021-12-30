package io.smallrye.health.registry;

import jakarta.enterprise.context.ApplicationScoped;

import org.eclipse.microprofile.health.Liveness;

import io.smallrye.health.api.HealthRegistry;

@Liveness
@ApplicationScoped
public class LivenessHealthRegistry extends AbstractHealthRegistry implements HealthRegistry {
}
