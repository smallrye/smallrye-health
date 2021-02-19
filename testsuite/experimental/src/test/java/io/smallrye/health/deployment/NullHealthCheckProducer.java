package io.smallrye.health.deployment;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Produces;

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
