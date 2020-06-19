package io.smallrye.health.deployment;

import javax.enterprise.context.ApplicationScoped;

import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Liveness;

@Liveness
@ApplicationScoped
public class SuccessLiveness implements HealthCheck {
    @Override
    public HealthCheckResponse call() {
        return HealthCheckResponse.up(SuccessLiveness.class.getName());
    }
}
