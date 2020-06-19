package io.smallrye.health.deployment;

import java.util.concurrent.atomic.AtomicInteger;

import javax.enterprise.context.ApplicationScoped;

import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Readiness;

import io.smallrye.health.api.AsyncHealthCheck;
import io.smallrye.mutiny.Uni;

@Readiness
@ApplicationScoped
public class ChangingReadinessHealthCheckAsync implements AsyncHealthCheck {

    private final AtomicInteger invocations = new AtomicInteger(0);

    @Override
    public Uni<HealthCheckResponse> call() {
        return Uni.createFrom()
                .item(invocations.getAndIncrement() == 0 ? HealthCheckResponse.up("up") : HealthCheckResponse.down("down"));
    }
}
