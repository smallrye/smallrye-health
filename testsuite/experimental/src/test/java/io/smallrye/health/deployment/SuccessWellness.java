package io.smallrye.health.deployment;

import jakarta.enterprise.context.ApplicationScoped;

import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;

import io.smallrye.health.api.Wellness;

@Wellness
@ApplicationScoped
public class SuccessWellness implements HealthCheck {
    @Override
    public HealthCheckResponse call() {
        return HealthCheckResponse.up(SuccessWellness.class.getName());
    }
}
