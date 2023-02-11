package io.smallrye.health.deployment;

import java.time.Duration;

import jakarta.enterprise.context.ApplicationScoped;

import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Liveness;

import io.smallrye.health.api.AsyncHealthCheck;
import io.smallrye.mutiny.Uni;

@Liveness
@ApplicationScoped
public class SuccessLivenessAsync implements AsyncHealthCheck {

    @Override
    public Uni<HealthCheckResponse> call() {
        return Uni.createFrom().item(HealthCheckResponse.up(SuccessLivenessAsync.class.getName()))
                .onItem().delayIt().by(Duration.ofMillis(10));
    }
}
