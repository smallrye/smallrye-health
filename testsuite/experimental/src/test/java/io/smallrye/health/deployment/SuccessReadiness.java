package io.smallrye.health.deployment;

import jakarta.enterprise.context.ApplicationScoped;

import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Readiness;

@Readiness
@ApplicationScoped
public class SuccessReadiness implements HealthCheck {
    @Override
    public HealthCheckResponse call() {
        return HealthCheckResponse.up(SuccessReadiness.class.getName());
    }
}
