package io.smallrye.health.deployment;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;

import io.smallrye.health.api.Wellness;

@Wellness
@ApplicationScoped
public class ContextHealthCheck implements HealthCheck {

    @Inject
    ContextInfo context;

    @Override
    public HealthCheckResponse call() {
        return HealthCheckResponse.up(context.getValue());
    }

}
