package io.smallrye.health.registry;

import jakarta.enterprise.context.ApplicationScoped;

import org.eclipse.microprofile.health.Readiness;

import io.smallrye.health.api.HealthRegistry;

@Readiness
@ApplicationScoped
public class ReadinessHealthRegistry extends AbstractHealthRegistry implements HealthRegistry {
}
