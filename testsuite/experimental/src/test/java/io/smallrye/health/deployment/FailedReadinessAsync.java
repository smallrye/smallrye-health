package io.smallrye.health.deployment;

import java.time.Duration;

import jakarta.enterprise.context.ApplicationScoped;

import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Readiness;

import io.smallrye.health.api.AsyncHealthCheck;
import io.smallrye.mutiny.Uni;

@Readiness
@ApplicationScoped
public class FailedReadinessAsync implements AsyncHealthCheck {

    @Override
    public Uni<HealthCheckResponse> call() {
        return Uni.createFrom().item(HealthCheckResponse.down(FailedReadinessAsync.class.getName()))
                .onItem().delayIt().by(Duration.ofMillis(10));
    }
}
