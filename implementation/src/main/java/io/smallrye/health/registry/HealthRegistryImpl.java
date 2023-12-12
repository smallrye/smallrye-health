package io.smallrye.health.registry;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;

import io.smallrye.health.AsyncHealthCheckFactory;
import io.smallrye.health.api.AsyncHealthCheck;
import io.smallrye.health.api.HealthRegistry;
import io.smallrye.mutiny.Uni;

public class HealthRegistryImpl implements HealthRegistry {

    Map<String, HealthCheck> checks = new HashMap<>();
    Map<String, AsyncHealthCheck> asyncChecks = new HashMap<>();
    private boolean checksChanged = false;

    AsyncHealthCheckFactory asyncHealthCheckFactory = new AsyncHealthCheckFactory();

    @Override
    public HealthRegistry register(String id, HealthCheck healthCheck) {
        return register(id, healthCheck, checks);
    }

    @Override
    public HealthRegistry register(String id, AsyncHealthCheck asyncHealthCheck) {
        return register(id, asyncHealthCheck, asyncChecks);
    }

    private <T> HealthRegistry register(String id, T check, Map<String, T> checks) {
        checks.put(id, check);
        checksChanged = true;
        return this;
    }

    @Override
    public HealthRegistry remove(String id) {
        try {
            if (checks.remove(id) == null && asyncChecks.remove(id) == null) {
                throw new IllegalStateException(String.format("ID '%s' not found", id));
            }
            checksChanged = true;
            return this;
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    public Collection<Uni<HealthCheckResponse>> getChecks(Map<String, Boolean> healthChecksConfigs) {
        var enabledChecks = checks.entrySet().stream()
                .filter(e -> healthChecksConfigs.getOrDefault(e.getKey(), true))
                .map(e -> asyncHealthCheckFactory.callSync(e.getValue()));
        var enabledAsyncChecks = asyncChecks.entrySet().stream()
                .filter(e -> healthChecksConfigs.getOrDefault(e.getKey(), true))
                .map(e -> asyncHealthCheckFactory.callAsync(e.getValue()));
        return Stream.concat(enabledChecks, enabledAsyncChecks).collect(Collectors.toList());
    }

    public boolean checksChanged() {
        return checksChanged;
    }
}
