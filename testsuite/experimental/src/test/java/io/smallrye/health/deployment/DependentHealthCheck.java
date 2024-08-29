package io.smallrye.health.deployment;

import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.Dependent;

import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Liveness;

@Dependent
@Liveness
public class DependentHealthCheck implements HealthCheck {

    @Override
    public org.eclipse.microprofile.health.HealthCheckResponse call() {
        return HealthCheckResponse.up(DependentHealthCheck.class.getName());
    }

    @PreDestroy
    public void preDestroy() {
        DependentCallRecorder.healthCheckPreDestroyCalled = true;
    }
}
