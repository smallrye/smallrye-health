package io.smallrye.health.registry;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import jakarta.inject.Inject;

import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;

import io.smallrye.health.AsyncHealthCheckFactory;
import io.smallrye.health.api.AsyncHealthCheck;
import io.smallrye.health.api.HealthRegistry;
import io.smallrye.mutiny.Uni;

public class AbstractHealthRegistry implements HealthRegistry {

    Map<String, Uni<HealthCheckResponse>> checks = new HashMap<>();
    private boolean checksChanged = false;

    @Inject
    AsyncHealthCheckFactory asyncHealthCheckFactory;

    @Override
    public HealthRegistry register(String id, HealthCheck healthCheck) {
        return register(id, asyncHealthCheckFactory.callSync(healthCheck));
    }

    @Override
    public HealthRegistry register(String id, AsyncHealthCheck asyncHealthCheck) {
        return register(id, asyncHealthCheckFactory.callAsync(asyncHealthCheck));
    }

    private HealthRegistry register(String id, Uni<HealthCheckResponse> healthCheckResponseUni) {
        try {
            checks.put(id, healthCheckResponseUni);
            checksChanged = true;
            return this;
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public HealthRegistry remove(String id) {
        try {
            if (checks.remove(id) == null) {
                throw new IllegalStateException(String.format("ID '%s' not found", id));
            }
            checksChanged = true;
            return this;
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    public Collection<Uni<HealthCheckResponse>> getChecks() {
        return Collections.unmodifiableCollection(checks.values());
    }

    public boolean checksChanged() {
        return checksChanged;
    }

    public void setAsyncHealthCheckFactory(AsyncHealthCheckFactory asyncHealthCheckFactory) {
        this.asyncHealthCheckFactory = asyncHealthCheckFactory;
    }
}
