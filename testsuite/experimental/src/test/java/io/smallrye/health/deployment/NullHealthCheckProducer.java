package io.smallrye.health.deployment;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Produces;

import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.Liveness;

@ApplicationScoped
public class NullHealthCheckProducer {

    @Produces
    @Dependent
    @Liveness
    public HealthCheck nullHealthCheck() {
        return null;
    }

}
