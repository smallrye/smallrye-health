package io.smallrye.health.deployment;

import jakarta.enterprise.context.ApplicationScoped;

import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;

import io.smallrye.health.api.HealthGroup;

@HealthGroup("health-group-1")
@ApplicationScoped
public class HealthGroup1 implements HealthCheck {

    @Override
    public HealthCheckResponse call() {
        return HealthCheckResponse.up("health-group-1");
    }
}
