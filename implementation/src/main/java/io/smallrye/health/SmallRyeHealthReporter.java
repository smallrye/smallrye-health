package io.smallrye.health;

import static io.smallrye.health.api.HealthType.LIVENESS;
import static io.smallrye.health.api.HealthType.READINESS;
import static io.smallrye.health.api.HealthType.STARTUP;
import static io.smallrye.health.api.HealthType.WELLNESS;

import java.io.OutputStream;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonWriter;
import javax.json.JsonWriterFactory;
import javax.json.spi.JsonProvider;
import javax.json.stream.JsonGenerator;

import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Liveness;
import org.eclipse.microprofile.health.Readiness;
import org.eclipse.microprofile.health.Startup;

import io.smallrye.common.annotation.Experimental;
import io.smallrye.config.SmallRyeConfig;
import io.smallrye.health.api.AsyncHealthCheck;
import io.smallrye.health.api.HealthGroup;
import io.smallrye.health.api.HealthType;
import io.smallrye.health.api.Wellness;
import io.smallrye.health.registry.HealthRegistries;
import io.smallrye.health.registry.HealthRegistryImpl;
import io.smallrye.mutiny.Uni;

@ApplicationScoped
public class SmallRyeHealthReporter {

    private static final Map<String, ?> JSON_CONFIG = Collections.singletonMap(JsonGenerator.PRETTY_PRINTING, true);

    /**
     * can be {@code null} if SmallRyeHealthReporter is used in a non-CDI environment
     */
    @Inject
    @Liveness
    Instance<HealthCheck> livenessChecks;

    @Inject
    @Readiness
    Instance<HealthCheck> readinessChecks;

    @Inject
    @Wellness
    Instance<HealthCheck> wellnessChecks;

    @Inject
    @Startup
    Instance<HealthCheck> startupChecks;

    @Inject
    @Any
    Instance<HealthCheck> allHealthChecks;

    @Inject
    @Liveness
    Instance<AsyncHealthCheck> asyncLivenessChecks;

    @Inject
    @Readiness
    Instance<AsyncHealthCheck> asyncReadinessChecks;

    @Inject
    @Wellness
    Instance<AsyncHealthCheck> asyncWellnessChecks;

    @Inject
    @Startup
    Instance<AsyncHealthCheck> asyncStartupChecks;

    @Inject
    @Any
    Instance<AsyncHealthCheck> allAsyncHealthChecks;

    @Inject
    BeanManager beanManager;

    HealthRegistryImpl livenessHealthRegistry = (HealthRegistryImpl) HealthRegistries.getRegistry(LIVENESS);
    HealthRegistryImpl readinessHealthRegistry = (HealthRegistryImpl) HealthRegistries.getRegistry(READINESS);
    HealthRegistryImpl wellnessHealthRegistry = (HealthRegistryImpl) HealthRegistries.getRegistry(WELLNESS);
    HealthRegistryImpl startupHealthRegistry = (HealthRegistryImpl) HealthRegistries.getRegistry(STARTUP);

    // Config properties
    boolean contextPropagated = false;
    String emptyChecksOutcome = "UP";
    int timeoutSeconds = 60;
    Map<String, String> additionalProperties = new HashMap<>();
    Map<String, Boolean> healthChecksConfigs = new HashMap<>();

    @Inject
    AsyncHealthCheckFactory asyncHealthCheckFactory;

    private final Map<String, Uni<HealthCheckResponse>> additionalChecks = new HashMap<>();

    private final JsonProvider jsonProvider = JsonProvider.provider();

    private Uni<SmallRyeHealth> smallRyeHealthUni = null;
    private Uni<SmallRyeHealth> smallRyeLivenessUni = null;
    private Uni<SmallRyeHealth> smallRyeReadinessUni = null;
    private Uni<SmallRyeHealth> smallryeWellnessUni = null;
    private Uni<SmallRyeHealth> smallryeStartupUni = null;
    private boolean additionalListsChanged = false;

    private List<Uni<HealthCheckResponse>> livenessUnis = new ArrayList<>();
    private List<Uni<HealthCheckResponse>> readinessUnis = new ArrayList<>();
    private List<Uni<HealthCheckResponse>> wellnessUnis = new ArrayList<>();
    private List<Uni<HealthCheckResponse>> startupUnis = new ArrayList<>();

    public SmallRyeHealthReporter() {
        try {
            contextPropagated = ConfigProvider.getConfig()
                    .getOptionalValue("io.smallrye.health.context.propagation", Boolean.class)
                    .orElse(false);

            emptyChecksOutcome = ConfigProvider.getConfig()
                    .getOptionalValue("io.smallrye.health.emptyChecksOutcome", String.class)
                    .orElse("UP");

            timeoutSeconds = ConfigProvider.getConfig()
                    .getOptionalValue("io.smallrye.health.timeout.seconds", Integer.class)
                    .orElse(60);

            additionalProperties = ((SmallRyeConfig) ConfigProvider.getConfig())
                    .getOptionalValues("io.smallrye.health.additional.property", String.class, String.class)
                    .orElse(new HashMap<>());
        } catch (IllegalStateException illegalStateException) {
            // OK, no config provider was found, use default values
        }

        if (asyncHealthCheckFactory == null) {
            asyncHealthCheckFactory = new AsyncHealthCheckFactory();
        }
    }

    @PostConstruct
    public void initChecks() {
        initUnis(livenessUnis, livenessChecks, asyncLivenessChecks);
        initUnis(readinessUnis, readinessChecks, asyncReadinessChecks);
        initUnis(wellnessUnis, wellnessChecks, asyncWellnessChecks);
        initUnis(startupUnis, startupChecks, asyncStartupChecks);
    }

    private void initUnis(List<Uni<HealthCheckResponse>> list, Iterable<HealthCheck> checks,
            Iterable<AsyncHealthCheck> asyncChecks) {
        if (checks != null) {
            for (HealthCheck check : checks) {
                if (check != null && isHealthCheckEnabled(check)) {
                    list.add(asyncHealthCheckFactory.callSync(check));
                }
            }
        }

        if (asyncChecks != null) {
            for (AsyncHealthCheck asyncCheck : asyncChecks) {
                if (asyncCheck != null && isHealthCheckEnabled(asyncCheck)) {
                    list.add(asyncHealthCheckFactory.callAsync(asyncCheck));
                }
            }
        }
    }

    public void reportHealth(OutputStream out, SmallRyeHealth health) {
        if (health.isDown() && HealthLogging.logger.isInfoEnabled()) {
            // Log reason, as not reported by container orchestrators, yet container may get killed.
            HealthLogging.logger.healthDownStatus(health.getPayload().toString());
        }

        JsonWriterFactory factory = jsonProvider.createWriterFactory(JSON_CONFIG);
        JsonWriter writer = factory.createWriter(out);

        writer.writeObject(health.getPayload());
        writer.close();
    }

    public SmallRyeHealth getHealth() {
        return getHealthAsync().await().atMost(Duration.ofSeconds(timeoutSeconds));
    }

    public SmallRyeHealth getLiveness() {
        return getLivenessAsync().await().atMost(Duration.ofSeconds(timeoutSeconds));
    }

    public SmallRyeHealth getReadiness() {
        return getReadinessAsync().await().atMost(Duration.ofSeconds(timeoutSeconds));
    }

    public SmallRyeHealth getStartup() {
        return getStartupAsync().await().atMost(Duration.ofSeconds(timeoutSeconds));
    }

    @Experimental("Wellness experimental checks")
    public SmallRyeHealth getWellness() {
        return getWellnessAsync().await().atMost(Duration.ofSeconds(timeoutSeconds));
    }

    public SmallRyeHealth getHealthGroup(String groupName) {
        return getHealthGroupAsync(groupName).await().atMost(Duration.ofSeconds(timeoutSeconds));
    }

    public SmallRyeHealth getHealthGroups() {
        return getHealthGroupsAsync().await().atMost(Duration.ofSeconds(timeoutSeconds));
    }

    @Experimental("Asynchronous Health Check procedures")
    public Uni<SmallRyeHealth> getHealthAsync() {
        smallRyeHealthUni = getHealthAsync(smallRyeHealthUni, LIVENESS, READINESS, WELLNESS, STARTUP);
        return smallRyeHealthUni;
    }

    @Experimental("Asynchronous Health Check procedures")
    public Uni<SmallRyeHealth> getLivenessAsync() {
        smallRyeLivenessUni = getHealthAsync(smallRyeLivenessUni, LIVENESS);
        return smallRyeLivenessUni;
    }

    @Experimental("Asynchronous Health Check procedures")
    public Uni<SmallRyeHealth> getReadinessAsync() {
        smallRyeReadinessUni = getHealthAsync(smallRyeReadinessUni, READINESS);
        return smallRyeReadinessUni;
    }

    @Experimental("Asynchronous Health Check procedures")
    public Uni<SmallRyeHealth> getStartupAsync() {
        smallryeStartupUni = getHealthAsync(smallryeStartupUni, STARTUP);
        return smallryeStartupUni;
    }

    @Experimental("Asynchronous Health Check procedures & wellness experimental checks")
    public Uni<SmallRyeHealth> getWellnessAsync() {
        smallryeWellnessUni = getHealthAsync(smallryeWellnessUni, WELLNESS);
        return smallryeWellnessUni;
    }

    @Experimental("Asynchronous Health Check procedures and Health Groups")
    public Uni<SmallRyeHealth> getHealthGroupAsync(String groupName) {
        List<Uni<HealthCheckResponse>> checks = new ArrayList<>();
        if (allHealthChecks != null && allAsyncHealthChecks != null) {
            initUnis(checks, allHealthChecks.select(HealthGroup.Literal.of(groupName)),
                    allAsyncHealthChecks.select(HealthGroup.Literal.of(groupName)));
        }

        checks.addAll(((HealthRegistryImpl) HealthRegistries.getHealthGroupRegistry(groupName)).getChecks(healthChecksConfigs));

        return getHealthAsync(checks);
    }

    @Experimental("Asynchronous Health Check procedures and Health Groups")
    public Uni<SmallRyeHealth> getHealthGroupsAsync() {
        List<Uni<HealthCheckResponse>> checks = new ArrayList<>();
        if (beanManager != null) {
            initUnis(checks, getHealthGroupsChecks(HealthCheck.class), getHealthGroupsChecks(AsyncHealthCheck.class));
        }

        HealthRegistries.getHealthGroupRegistries()
                .forEach(healthRegistry -> checks.addAll(((HealthRegistryImpl) healthRegistry).getChecks(healthChecksConfigs)));

        return getHealthAsync(checks);
    }

    public void addHealthCheck(HealthCheck check) {
        if (check != null) {
            additionalChecks.put(check.getClass().getName(), asyncHealthCheckFactory.callSync(check));
            additionalListsChanged = true;
        }
    }

    public void addHealthCheck(AsyncHealthCheck check) {
        if (check != null) {
            additionalChecks.put(check.getClass().getName(), asyncHealthCheckFactory.callAsync(check));
            additionalListsChanged = true;
        }
    }

    public void removeHealthCheck(HealthCheck check) {
        additionalChecks.remove(check.getClass().getName());
        additionalListsChanged = true;
    }

    public void removeHealthCheck(AsyncHealthCheck check) {
        additionalChecks.remove(check.getClass().getName());
        additionalListsChanged = true;
    }

    // Manual config overrides

    public void setContextPropagated(boolean contextPropagated) {
        this.contextPropagated = contextPropagated;
    }

    public void setEmptyChecksOutcome(String emptyChecksOutcome) {
        Objects.requireNonNull(emptyChecksOutcome);
        this.emptyChecksOutcome = emptyChecksOutcome;
    }

    public void setTimeoutSeconds(int timeoutSeconds) {
        if (timeoutSeconds < 0) {
            throw new IllegalArgumentException("Timeout cannot be negative.");
        }
        this.timeoutSeconds = timeoutSeconds;
    }

    public void setAdditionalProperties(Map<String, String> additionalProperties) {
        Objects.requireNonNull(additionalProperties);
        this.additionalProperties = new HashMap<>(additionalProperties);
    }

    public void setHealthChecksConfigs(Map<String, Boolean> healthChecksConfigs) {
        Objects.requireNonNull(healthChecksConfigs);
        this.healthChecksConfigs = new HashMap<>(healthChecksConfigs);
        recreateCheckUnis();
    }

    @SuppressWarnings("unchecked")
    private <T> List<T> getHealthGroupsChecks(Class<T> checkClass) {
        Iterator<Bean<?>> iterator = beanManager.getBeans(checkClass, Any.Literal.INSTANCE).iterator();

        List<T> groupHealthChecks = new ArrayList<>();

        while (iterator.hasNext()) {
            Bean<?> bean = iterator.next();
            if (bean.getQualifiers().stream().anyMatch(annotation -> annotation.annotationType().equals(HealthGroup.class))) {
                groupHealthChecks.add((T) beanManager.getReference(bean, bean.getBeanClass(),
                        beanManager.createCreationalContext(bean)));
            }
        }

        return groupHealthChecks;
    }

    private Uni<SmallRyeHealth> getHealthAsync(Uni<SmallRyeHealth> cachedHealth, HealthType... types) {
        if (contextPropagated) {
            recreateCheckUnis();
            return computeHealth(types);
        } else {
            if (additionalListsChanged(types) || additionalListsChanged || cachedHealth == null) {
                additionalListsChanged = false;
                cachedHealth = computeHealth(types);
            }

            return cachedHealth;
        }
    }

    private void recreateCheckUnis() {
        livenessUnis.clear();
        readinessUnis.clear();
        wellnessUnis.clear();
        startupUnis.clear();

        initChecks();
    }

    private boolean additionalListsChanged(HealthType... types) {
        boolean needRecompute = false;
        for (HealthType type : types) {
            switch (type) {
                case LIVENESS:
                    if (livenessHealthRegistry.checksChanged()) {
                        needRecompute = true;
                    }
                    break;
                case READINESS:
                    if (readinessHealthRegistry.checksChanged()) {
                        needRecompute = true;
                    }
                    break;
                case WELLNESS:
                    if (wellnessHealthRegistry.checksChanged()) {
                        needRecompute = true;
                    }
                    break;
                case STARTUP:
                    if (startupHealthRegistry.checksChanged()) {
                        needRecompute = true;
                    }
            }
        }

        return needRecompute;
    }

    private Uni<SmallRyeHealth> computeHealth(HealthType[] types) {
        List<Uni<HealthCheckResponse>> checks = new ArrayList<>();

        for (HealthType type : types) {
            switch (type) {
                case LIVENESS:
                    checks.addAll(livenessUnis);
                    checks.addAll(livenessHealthRegistry.getChecks(healthChecksConfigs));
                    break;
                case READINESS:
                    checks.addAll(readinessUnis);
                    checks.addAll(readinessHealthRegistry.getChecks(healthChecksConfigs));
                    break;
                case WELLNESS:
                    checks.addAll(wellnessUnis);
                    checks.addAll(wellnessHealthRegistry.getChecks(healthChecksConfigs));
                    break;
                case STARTUP:
                    checks.addAll(startupUnis);
                    checks.addAll(startupHealthRegistry.getChecks(healthChecksConfigs));
                    break;
            }
        }

        return getHealthAsync(checks);
    }

    private Uni<SmallRyeHealth> getHealthAsync(Collection<Uni<HealthCheckResponse>> checks) {
        List<Uni<HealthCheckResponse>> healthCheckUnis = new ArrayList<>();

        if (checks != null) {
            healthCheckUnis.addAll(checks);
        }

        if (!additionalChecks.isEmpty()) {
            healthCheckUnis.addAll(additionalChecks.values());
        }

        if (healthCheckUnis.isEmpty()) {
            return Uni.createFrom().item(createEmptySmallRyeHealth(emptyChecksOutcome));
        }

        return Uni.combine().all().unis(healthCheckUnis)
                .combinedWith(responses -> {
                    JsonArrayBuilder results = jsonProvider.createArrayBuilder();
                    HealthCheckResponse.Status status = HealthCheckResponse.Status.UP;

                    for (Object o : responses) {
                        HealthCheckResponse response = (HealthCheckResponse) o;
                        status = handleResponse(response, results, status);
                    }

                    return createSmallRyeHealth(results, status, emptyChecksOutcome);
                });
    }

    private SmallRyeHealth createEmptySmallRyeHealth(String emptyOutcome) {
        return createSmallRyeHealth(jsonProvider.createArrayBuilder(), null, emptyOutcome);
    }

    private SmallRyeHealth createSmallRyeHealth(JsonArrayBuilder results, HealthCheckResponse.Status status,
            String emptyOutcome) {
        JsonObjectBuilder builder = jsonProvider.createObjectBuilder();
        JsonArray checkResults = results.build();

        builder.add("status", checkResults.isEmpty() ? emptyOutcome : status.toString());
        builder.add("checks", checkResults);

        if (!additionalProperties.isEmpty()) {
            additionalProperties.forEach(builder::add);
        }

        return new SmallRyeHealth(builder.build());
    }

    private HealthCheckResponse.Status handleResponse(HealthCheckResponse response, JsonArrayBuilder results,
            HealthCheckResponse.Status globalOutcome) {
        JsonObject responseJson = jsonObject(response);
        results.add(responseJson);

        if (globalOutcome == HealthCheckResponse.Status.UP) {
            String status = responseJson.getString("status");
            if (status.equals("DOWN")) {
                return HealthCheckResponse.Status.DOWN;
            }
        }

        return globalOutcome;
    }

    private JsonObject jsonObject(HealthCheckResponse response) {
        JsonObjectBuilder builder = jsonProvider.createObjectBuilder();
        builder.add("name", response.getName());
        builder.add("status", response.getStatus().toString());
        response.getData().ifPresent(d -> {
            JsonObjectBuilder data = jsonProvider.createObjectBuilder();
            for (Map.Entry<String, Object> entry : d.entrySet()) {
                Object value = entry.getValue();
                if (value instanceof String) {
                    data.add(entry.getKey(), (String) value);
                } else if (value instanceof Long) {
                    data.add(entry.getKey(), (Long) value);
                } else if (value instanceof Boolean) {
                    data.add(entry.getKey(), (Boolean) value);
                }
            }
            builder.add("data", data.build());
        });

        return builder.build();
    }

    private boolean isHealthCheckEnabled(HealthCheck healthCheck) {
        return isEnabled(healthCheck.getClass().getName()) &&
                isEnabled(healthCheck.getClass().getSuperclass().getName());
    }

    private boolean isHealthCheckEnabled(AsyncHealthCheck asyncHealthCheck) {
        return isEnabled(asyncHealthCheck.getClass().getName()) &&
                isEnabled(asyncHealthCheck.getClass().getSuperclass().getName());
    }

    private boolean isEnabled(String checkClassName) {
        return healthChecksConfigs.computeIfAbsent(checkClassName, s -> {
            try {
                return ConfigProvider.getConfig()
                        .getOptionalValue("io.smallrye.health.check." + checkClassName + ".enabled", Boolean.class)
                        .orElse(true);
            } catch (IllegalStateException illegalStateException) {
                // OK, no config provider was found, use default values
            }

            return true;
        });

    }
}
