package io.smallrye.health.deployment;

import javax.enterprise.context.ApplicationScoped;

import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;

import io.smallrye.health.api.HealthGroup;

@HealthGroup("health-group-1")
@HealthGroup("health-group-2")
@ApplicationScoped
public class HealthGroup12 implements HealthCheck {

    @Override
    public HealthCheckResponse call() {
        return HealthCheckResponse.up("health-group-12");
    }
}
