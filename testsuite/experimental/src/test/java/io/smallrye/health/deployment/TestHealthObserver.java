package io.smallrye.health.deployment;

import static org.eclipse.microprofile.health.HealthCheckResponse.Status.UP;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.Default;

import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Liveness;
import org.eclipse.microprofile.health.Readiness;
import org.eclipse.microprofile.health.Startup;

import io.smallrye.health.api.Wellness;
import io.smallrye.health.api.event.HealthStatusChangeEvent;

@ApplicationScoped
public class TestHealthObserver {

    private int counterHealth;
    private int counterLiveness;
    private int counterReadiness;
    private int counterWellness;
    private int counterStartup;

    private HealthCheckResponse.Status healthStatus = UP;
    private HealthCheckResponse.Status livenessStatus = UP;
    private HealthCheckResponse.Status readinessStatus = UP;
    private HealthCheckResponse.Status wellnessStatus = UP;
    private HealthCheckResponse.Status startupStatus = UP;

    public void observeHealth(@Observes @Default HealthStatusChangeEvent event) {
        counterHealth++;
        healthStatus = event.status();
    }

    public void observeLiveness(@Observes @Liveness HealthStatusChangeEvent event) {
        counterLiveness++;
        livenessStatus = event.status();
    }

    public void observeReadiness(@Observes @Readiness HealthStatusChangeEvent event) {
        counterReadiness++;
        readinessStatus = event.status();
    }

    public void observeWellness(@Observes @Wellness HealthStatusChangeEvent event) {
        counterWellness++;
        wellnessStatus = event.status();
    }

    public void observeStartup(@Observes @Startup HealthStatusChangeEvent event) {
        counterStartup++;
        startupStatus = event.status();
    }

    public int getCounterHealth() {
        return counterHealth;
    }

    public int getCounterLiveness() {
        return counterLiveness;
    }

    public int getCounterReadiness() {
        return counterReadiness;
    }

    public int getCounterWellness() {
        return counterWellness;
    }

    public int getCounterStartup() {
        return counterStartup;
    }

    public HealthCheckResponse.Status getHealthStatus() {
        return healthStatus;
    }

    public HealthCheckResponse.Status getLivenessStatus() {
        return livenessStatus;
    }

    public HealthCheckResponse.Status getReadinessStatus() {
        return readinessStatus;
    }

    public HealthCheckResponse.Status getWellnessStatus() {
        return wellnessStatus;
    }

    public HealthCheckResponse.Status getStartupStatus() {
        return startupStatus;
    }
}
