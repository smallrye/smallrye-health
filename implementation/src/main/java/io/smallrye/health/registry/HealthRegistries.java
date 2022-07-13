package io.smallrye.health.registry;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.health.Liveness;
import org.eclipse.microprofile.health.Readiness;
import org.eclipse.microprofile.health.Startup;

import io.smallrye.health.api.HealthRegistry;
import io.smallrye.health.api.HealthType;
import io.smallrye.health.api.Wellness;

@ApplicationScoped
public class HealthRegistries {

    private static final int MAX_GROUP_REGISTRIES_COUNT_DEFAULT = 100;

    private static final Map<HealthType, HealthRegistry> registries = new ConcurrentHashMap<>();
    private static final Map<String, HealthRegistry> groupRegistries = new ConcurrentHashMap<>();

    private static int maxGroupRegistriesCount = MAX_GROUP_REGISTRIES_COUNT_DEFAULT;

    static {
        try {
            maxGroupRegistriesCount = ConfigProvider.getConfig()
                    .getOptionalValue("io.smallrye.health.maxGroupRegistriesCount", Integer.class)
                    .orElse(MAX_GROUP_REGISTRIES_COUNT_DEFAULT);
        } catch (IllegalStateException illegalStateException) {
            // OK, no config provider was found, use default values
        }
    }

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

        HealthRegistry healthRegistry = groupRegistries.get(groupName);
        if (healthRegistry == null) {
            if (groupRegistries.keySet().size() >= maxGroupRegistriesCount) {
                throw new IllegalStateException(
                        "Attempted to create more custom health group registries than allowed: " + maxGroupRegistriesCount);
            }

            healthRegistry = groupRegistries.computeIfAbsent(groupName, s -> new HealthRegistryImpl());
        }

        return healthRegistry;
    }

    public static Collection<HealthRegistry> getHealthGroupRegistries() {
        return Collections.unmodifiableCollection(groupRegistries.values());
    }
}
