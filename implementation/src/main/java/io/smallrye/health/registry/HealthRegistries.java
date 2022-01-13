package io.smallrye.health.registry;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

import org.eclipse.microprofile.health.Liveness;
import org.eclipse.microprofile.health.Readiness;
import org.eclipse.microprofile.health.Startup;

import io.smallrye.health.api.HealthRegistry;
import io.smallrye.health.api.HealthType;
import io.smallrye.health.api.Wellness;

@ApplicationScoped
public class HealthRegistries {

    private static final Map<HealthType, HealthRegistry> registries = new ConcurrentHashMap<>();
    private static final Map<String, HealthRegistry> groupRegistries = new ConcurrentHashMap<>();

    @Produces
    @Liveness
    @ApplicationScoped
    public HealthRegistry getLivenessRegistry() {
        return getRegistry(HealthType.LIVENESS);
    }

    @Produces
    @Readiness
    @ApplicationScoped
    public HealthRegistry getReadinessRegistry() {
        return getRegistry(HealthType.READINESS);
    }

    @Produces
    @Startup
    @ApplicationScoped
    public HealthRegistry getStartupRegistry() {
        return getRegistry(HealthType.STARTUP);
    }

    @Produces
    @Wellness
    @ApplicationScoped
    public HealthRegistry getWellnessRegistry() {
        return getRegistry(HealthType.WELLNESS);
    }

    public static HealthRegistry getRegistry(HealthType type) {
        return registries.computeIfAbsent(type, t -> new HealthRegistryImpl());
    }

    public static HealthRegistry getHealthGroupRegistry(String groupName) {
        if (groupName == null) {
            throw new IllegalArgumentException("Health group name cannot be null");
        }

        return groupRegistries.computeIfAbsent(groupName, s -> new HealthRegistryImpl());
    }

    public static Collection<HealthRegistry> getHealthGroupRegistries() {
        return Collections.unmodifiableCollection(groupRegistries.values());
    }
}
