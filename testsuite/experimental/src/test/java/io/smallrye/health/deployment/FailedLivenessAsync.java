package io.smallrye.health.deployment;

import java.time.Duration;

import javax.enterprise.context.ApplicationScoped;

import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Liveness;

import io.smallrye.health.api.AsyncHealthCheck;
import io.smallrye.mutiny.Uni;

@Liveness
@ApplicationScoped
public class FailedLivenessAsync implements AsyncHealthCheck {

    @Override
    public Uni<HealthCheckResponse> call() {
        return Uni.createFrom().item(HealthCheckResponse.down(FailedLivenessAsync.class.getName()))
                .onItem().delayIt().by(Duration.ofMillis(10));
    }
}
