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
import io.smallrye.health.event.SmallRyeHealthStatusChangeEvent;

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

    public void observeHealth(@Observes @Default SmallRyeHealthStatusChangeEvent event) {
        counterHealth++;
        healthStatus = event.health().getStatus();
    }

    public void observeLiveness(@Observes @Liveness SmallRyeHealthStatusChangeEvent event) {
        counterLiveness++;
        livenessStatus = event.health().getStatus();
    }

    public void observeReadiness(@Observes @Readiness SmallRyeHealthStatusChangeEvent event) {
        counterReadiness++;
        readinessStatus = event.health().getStatus();
    }

    public void observeWellness(@Observes @Wellness SmallRyeHealthStatusChangeEvent event) {
        counterWellness++;
        wellnessStatus = event.health().getStatus();
    }

    public void observeStartup(@Observes @Startup SmallRyeHealthStatusChangeEvent event) {
        counterStartup++;
        startupStatus = event.health().getStatus();
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
