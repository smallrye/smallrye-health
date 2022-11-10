package io.smallrye.health.deployment;

import javax.enterprise.context.ApplicationScoped;

import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;

import io.smallrye.health.api.HealthGroup;

@HealthGroup("default-group")
@ApplicationScoped
public class DefaultGroupExplicit implements HealthCheck {

    @Override
    public HealthCheckResponse call() {
        return HealthCheckResponse.up(DefaultGroupExplicit.class.getName());
    }
}
