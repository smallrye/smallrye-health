package io.smallrye.health.deployment;

import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.Dependent;

import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Liveness;

import io.smallrye.health.api.AsyncHealthCheck;
import io.smallrye.mutiny.Uni;

@Dependent
@Liveness
public class AsyncDependentHealthCheck implements AsyncHealthCheck {

    @Override
    public Uni<HealthCheckResponse> call() {
        return Uni.createFrom().item(HealthCheckResponse.up(AsyncDependentHealthCheck.class.getName()));
    }

    @PreDestroy
    public void preDestroy() {
        DependentCallRecorder.asyncHealthCheckPreDestroyCalled = true;
    }
}
