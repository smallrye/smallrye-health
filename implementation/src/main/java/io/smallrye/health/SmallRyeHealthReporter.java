package io.smallrye.health;

import static io.smallrye.health.api.HealthType.LIVENESS;
import static io.smallrye.health.api.HealthType.READINESS;
import static io.smallrye.health.api.HealthType.STARTUP;
import static io.smallrye.health.api.HealthType.WELLNESS;
import static org.eclipse.microprofile.health.HealthCheckResponse.Status.DOWN;
import static org.eclipse.microprofile.health.HealthCheckResponse.Status.UP;

import java.io.OutputStream;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.event.Event;
import jakarta.enterprise.event.ObserverException;
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.Default;
import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.inject.Inject;
import jakarta.json.JsonArray;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.json.JsonWriter;
import jakarta.json.JsonWriterFactory;
import jakarta.json.spi.JsonProvider;
import jakarta.json.stream.JsonGenerator;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Liveness;
import org.eclipse.microprofile.health.Readiness;
import org.eclipse.microprofile.health.Startup;

import io.smallrye.common.annotation.Experimental;
import io.smallrye.config.SmallRyeConfig;
import io.smallrye.health.api.AsyncHealthCheck;
import io.smallrye.health.api.HealthContentFilter;
import io.smallrye.health.api.HealthGroup;
import io.smallrye.health.api.HealthType;
import io.smallrye.health.api.Wellness;
import io.smallrye.health.event.SmallRyeHealthStatusChangeEvent;
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
    @Any
    Instance<HealthContentFilter> healthContentFilters;

    @Inject
    BeanManager beanManager;

    HealthRegistryImpl livenessHealthRegistry = (HealthRegistryImpl) HealthRegistries.getRegistry(LIVENESS);
    HealthRegistryImpl readinessHealthRegistry = (HealthRegistryImpl) HealthRegistries.getRegistry(READINESS);
    HealthRegistryImpl wellnessHealthRegistry = (HealthRegistryImpl) HealthRegistries.getRegistry(WELLNESS);
    HealthRegistryImpl startupHealthRegistry = (HealthRegistryImpl) HealthRegistries.getRegistry(STARTUP);

    // Config properties
    HealthCheckResponse.Status emptyChecksOutcome = UP;
    int timeoutSeconds = 60;
    Map<String, String> additionalProperties = new ConcurrentHashMap<>();
    Map<String, Boolean> healthChecksConfigs = new ConcurrentHashMap<>();
    boolean delayHealthCheckInit = false;
    String defaultHealthGroup = null;

    @Inject
    AsyncHealthCheckFactory asyncHealthCheckFactory;

    private final Map<String, Uni<HealthCheckResponse>> additionalChecks = new HashMap<>();

    private static final JsonProvider JSON_PROVIDER = JsonProvider.provider();
    private volatile boolean checksInitialized = false;

    private HealthCheckResponse.Status healthStatus = UP;
    private HealthCheckResponse.Status livenessStatus = UP;
    private HealthCheckResponse.Status readinessStatus = UP;
    private HealthCheckResponse.Status startupStatus = UP;
    private HealthCheckResponse.Status wellnessStatus = UP;

    private List<Uni<HealthCheckResponse>> livenessUnis = new CopyOnWriteArrayList<>();
    private List<Uni<HealthCheckResponse>> readinessUnis = new CopyOnWriteArrayList<>();
    private List<Uni<HealthCheckResponse>> wellnessUnis = new CopyOnWriteArrayList<>();
    private List<Uni<HealthCheckResponse>> startupUnis = new CopyOnWriteArrayList<>();

    public SmallRyeHealthReporter() {
        try {
            Config config = ConfigProvider.getConfig();
            emptyChecksOutcome = config
                    .getOptionalValue("io.smallrye.health.emptyChecksOutcome", HealthCheckResponse.Status.class).orElse(UP);

            timeoutSeconds = config.getOptionalValue("io.smallrye.health.timeout.seconds", Integer.class).orElse(60);

            additionalProperties = ((SmallRyeConfig) config)
                    .getOptionalValues("io.smallrye.health.additional.property", String.class, String.class)
                    .orElse(new ConcurrentHashMap<>());

            delayHealthCheckInit = config.getOptionalValue("io.smallrye.health.delayChecksInitializations", Boolean.class)
                    .orElse(false);

            defaultHealthGroup = config.getOptionalValue("io.smallrye.health.defaultHealthGroup", String.class).orElse(null);
        } catch (IllegalStateException illegalStateException) {
            // OK, no config provider was found, use default values
        }

        if (!delayHealthCheckInit) {
            initChecks();
        }

        try {
            asyncHealthCheckFactory = CDI.current().select(AsyncHealthCheckFactory.class).get();
        } catch (Exception e) {
            // CDI not available, use default
            asyncHealthCheckFactory = new AsyncHealthCheckFactory();
        }
    }

    private synchronized void initChecks() {
        if (checksInitialized) {
            return;
        }
        initUnis(livenessUnis, livenessChecks, asyncLivenessChecks);
        initUnis(readinessUnis, readinessChecks, asyncReadinessChecks);
        initUnis(wellnessUnis, wellnessChecks, asyncWellnessChecks);
        initUnis(startupUnis, startupChecks, asyncStartupChecks);
        checksInitialized = true;
    }

    private void initUnis(List<Uni<HealthCheckResponse>> list, Instance<HealthCheck> checks,
            Instance<AsyncHealthCheck> asyncChecks) {
        if (checks != null) {
            for (Instance.Handle<HealthCheck> handle : checks.handles()) {
                HealthCheck check = handle.get();
                if (check != null && isHealthCheckEnabled(check)) {
                    list.add(asyncHealthCheckFactory.callSync(check).chain(response -> {
                        if (handle.getBean().getScope().equals(Dependent.class)) {
                            handle.destroy();
                        }
                        return Uni.createFrom().item(response);
                    }));
                }
            }
        }

        if (asyncChecks != null) {
            for (Instance.Handle<AsyncHealthCheck> handle : asyncChecks.handles()) {
                AsyncHealthCheck asyncCheck = handle.get();
                if (asyncCheck != null && isHealthCheckEnabled(asyncCheck)) {
                    list.add(asyncHealthCheckFactory.callAsync(asyncCheck).chain(response -> {
                        if (handle.getBean().getScope().equals(Dependent.class)) {
                            handle.destroy();
                        }
                        return Uni.createFrom().item(response);
                    }));
                }
            }
        }
    }

    // TODO this means we cannot have Dependent HealthGroups checks
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

    private List<Uni<HealthCheckResponse>> recreateUnis(Instance<HealthCheck> checks, Instance<AsyncHealthCheck> asyncChecks) {
        List<Uni<HealthCheckResponse>> list = new ArrayList<>();
        initUnis(list, checks, asyncChecks);
        return list;
    }

    public void reportHealth(OutputStream out, SmallRyeHealth health) {
        JsonObject payload = health.getPayload();
        if (health.isDown() && HealthLogging.logger.isInfoEnabled()) {
            // Log reason, as not reported by container orchestrators, yet container may get killed.
            HealthLogging.logger.healthDownStatus(payload.toString());
        }

        if (healthContentFilters != null) {
            for (Instance.Handle<HealthContentFilter> handle : healthContentFilters.handles()) {
                try {
                    payload = handle.get().filter(payload);
                } finally {
                    if (handle.getBean().getScope().equals(Dependent.class)) {
                        handle.destroy();
                    }
                }
            }
        }

        JsonWriterFactory factory = JSON_PROVIDER.createWriterFactory(JSON_CONFIG);
        JsonWriter writer = factory.createWriter(out);

        writer.writeObject(payload);
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

    @Inject
    @Default
    Event<SmallRyeHealthStatusChangeEvent> healthEvent;

    @Inject
    @Liveness
    Event<SmallRyeHealthStatusChangeEvent> livenessEvent;

    @Inject
    @Readiness
    Event<SmallRyeHealthStatusChangeEvent> readinessEvent;

    @Inject
    @Wellness
    Event<SmallRyeHealthStatusChangeEvent> wellnessEvent;

    @Inject
    @Startup
    Event<SmallRyeHealthStatusChangeEvent> startupEvent;

    @Experimental("Asynchronous Health Check procedures")
    public Uni<SmallRyeHealth> getHealthAsync() {
        return getHealthAsync(LIVENESS, READINESS, WELLNESS, STARTUP);
    }

    @Experimental("Asynchronous Health Check procedures")
    public Uni<SmallRyeHealth> getLivenessAsync() {
        return getHealthAsync(LIVENESS);
    }

    @Experimental("Asynchronous Health Check procedures")
    public Uni<SmallRyeHealth> getReadinessAsync() {
        return getHealthAsync(READINESS);
    }

    @Experimental("Asynchronous Health Check procedures")
    public Uni<SmallRyeHealth> getStartupAsync() {
        return getHealthAsync(STARTUP);
    }

    @Experimental("Asynchronous Health Check procedures & wellness experimental checks")
    public Uni<SmallRyeHealth> getWellnessAsync() {
        return getHealthAsync(WELLNESS);
    }

    @Experimental("Asynchronous Health Check procedures and Health Groups")
    public Uni<SmallRyeHealth> getHealthGroupAsync(String groupName) {
        List<Uni<HealthCheckResponse>> checks = new ArrayList<>();
        if (allHealthChecks != null && allAsyncHealthChecks != null) {
            if (groupName.equals(defaultHealthGroup)) {
                initUnis(checks, getHealthChecksWithoutHealthGroup(HealthCheck.class),
                        getHealthChecksWithoutHealthGroup(AsyncHealthCheck.class));
            }

            initUnis(checks, allHealthChecks.select(HealthGroup.Literal.of(groupName)),
                    allAsyncHealthChecks.select(HealthGroup.Literal.of(groupName)));
        }

        checks.addAll(((HealthRegistryImpl) HealthRegistries.getHealthGroupRegistry(groupName)).getChecks(healthChecksConfigs));

        return getHealthResult(checks).map(HealthResult::toSmallRyeHealth);
    }

    @Experimental("Asynchronous Health Check procedures and Health Groups")
    public Uni<SmallRyeHealth> getHealthGroupsAsync() {
        List<Uni<HealthCheckResponse>> checks = new ArrayList<>();

        if (defaultHealthGroup != null) {
            // all checks are either in some HealthGroup or they are in the default HealthGroup
            // so we can return all checks
            initUnis(checks, allHealthChecks, allAsyncHealthChecks);
        } else {
            if (beanManager != null) {
                initUnis(checks, getHealthGroupsChecks(HealthCheck.class), getHealthGroupsChecks(AsyncHealthCheck.class));
            }

            HealthRegistries.getHealthGroupRegistries().forEach(
                    healthRegistry -> checks.addAll(((HealthRegistryImpl) healthRegistry).getChecks(healthChecksConfigs)));
        }

        return getHealthResult(checks).map(HealthResult::toSmallRyeHealth);
    }

    public void addHealthCheck(HealthCheck check) {
        if (check != null) {
            additionalChecks.put(check.getClass().getName(), asyncHealthCheckFactory.callSync(check));
        }
    }

    public void addHealthCheck(AsyncHealthCheck check) {
        if (check != null) {
            additionalChecks.put(check.getClass().getName(), asyncHealthCheckFactory.callAsync(check));
        }
    }

    public void removeHealthCheck(HealthCheck check) {
        additionalChecks.remove(check.getClass().getName());
    }

    public void removeHealthCheck(AsyncHealthCheck check) {
        additionalChecks.remove(check.getClass().getName());
    }

    // Manual config overrides

    public void setEmptyChecksOutcome(String emptyChecksOutcome) {
        Objects.requireNonNull(emptyChecksOutcome);
        this.emptyChecksOutcome = HealthCheckResponse.Status.valueOf(emptyChecksOutcome);
    }

    public void setTimeoutSeconds(int timeoutSeconds) {
        if (timeoutSeconds < 0) {
            throw new IllegalArgumentException("Timeout cannot be negative.");
        }
        this.timeoutSeconds = timeoutSeconds;
    }

    public void setAdditionalProperties(Map<String, String> additionalProperties) {
        Objects.requireNonNull(additionalProperties);
        this.additionalProperties = new ConcurrentHashMap<>(additionalProperties);
    }

    public void setHealthChecksConfigs(Map<String, Boolean> healthChecksConfigs) {
        Objects.requireNonNull(healthChecksConfigs);
        this.healthChecksConfigs = new ConcurrentHashMap<>(healthChecksConfigs);
        checksInitialized = false;
    }

    @SuppressWarnings("unchecked")
    private <T> List<T> getHealthGroupsChecks(Class<T> checkClass) {
        Iterator<Bean<?>> iterator = beanManager.getBeans(checkClass, Any.Literal.INSTANCE).iterator();

        List<T> groupHealthChecks = new ArrayList<>();

        while (iterator.hasNext()) {
            Bean<?> bean = iterator.next();
            if (bean.getQualifiers().stream().anyMatch(annotation -> annotation.annotationType().equals(HealthGroup.class))) {
                groupHealthChecks.add(
                        (T) beanManager.getReference(bean, bean.getBeanClass(), beanManager.createCreationalContext(bean)));
            }
        }

        return groupHealthChecks;
    }

    @SuppressWarnings("unchecked")
    private <T> List<T> getHealthChecksWithoutHealthGroup(Class<T> checkClass) {
        Iterator<Bean<?>> iterator = beanManager.getBeans(checkClass, Any.Literal.INSTANCE).iterator();

        List<T> healthChecks = new ArrayList<>();

        while (iterator.hasNext()) {
            Bean<?> bean = iterator.next();
            if (bean.getQualifiers().stream().noneMatch(annotation -> annotation.annotationType().equals(HealthGroup.class))) {
                healthChecks.add(
                        (T) beanManager.getReference(bean, bean.getBeanClass(), beanManager.createCreationalContext(bean)));
            }
        }

        return healthChecks;
    }

    private Uni<SmallRyeHealth> getHealthAsync(HealthType... types) {
        Map<HealthType, Uni<HealthResult>> healthResults = new EnumMap<>(HealthType.class);

        for (HealthType type : types) {
            switch (type) {
                case LIVENESS:
                    healthResults.put(LIVENESS, getHealthResult(recreateUnis(livenessChecks, asyncLivenessChecks),
                            livenessHealthRegistry.getChecks(healthChecksConfigs)));
                    break;
                case READINESS:
                    healthResults.put(READINESS, getHealthResult(recreateUnis(readinessChecks, asyncReadinessChecks),
                            readinessHealthRegistry.getChecks(healthChecksConfigs)));
                    break;
                case WELLNESS:
                    healthResults.put(WELLNESS, getHealthResult(recreateUnis(wellnessChecks, asyncWellnessChecks),
                            wellnessHealthRegistry.getChecks(healthChecksConfigs)));
                    break;
                case STARTUP:
                    healthResults.put(STARTUP, getHealthResult(recreateUnis(startupChecks, asyncStartupChecks),
                            startupHealthRegistry.getChecks(healthChecksConfigs)));
                    break;
            }
        }

        return processHealthResults(healthResults);
    }

    private Uni<HealthResult> emptyHealthResult() {
        return Uni.createFrom().item(new HealthResult(emptyChecksOutcome));
    }

    private Uni<SmallRyeHealth> processHealthResults(Map<HealthType, Uni<HealthResult>> healthResults) {
        Uni<HealthResult> additionalHealthResult = emptyHealthResult();
        if (!additionalChecks.isEmpty()) {
            additionalHealthResult = getHealthResult(additionalChecks.values());
        }

        // Need to use Uni.join() because Uni.combine() has a performance issue - https://github.com/smallrye/smallrye-mutiny/issues/1993
        return Uni.join().all(
                healthResults.getOrDefault(LIVENESS, emptyHealthResult()),
                healthResults.getOrDefault(READINESS, emptyHealthResult()),
                healthResults.getOrDefault(WELLNESS, emptyHealthResult()),
                healthResults.getOrDefault(STARTUP, emptyHealthResult()),
                additionalHealthResult)
                .andCollectFailures()
                .map(resultList -> {
                    HealthResult result = new HealthResult();

                    livenessStatus = handleHealthResult(resultList.get(0), LIVENESS, livenessEvent, livenessStatus, result);
                    readinessStatus = handleHealthResult(resultList.get(1), READINESS, readinessEvent, readinessStatus, result);
                    wellnessStatus = handleHealthResult(resultList.get(2), WELLNESS, wellnessEvent, wellnessStatus, result);
                    startupStatus = handleHealthResult(resultList.get(3), STARTUP, startupEvent, startupStatus, result);

                    HealthResult additionalChecks = resultList.get(4);

                    if (!additionalChecks.checks.isEmpty()) {
                        result.checks.addAll(additionalChecks.checks);

                        if (result.status == UP && additionalChecks.status == DOWN) {
                            result.status = DOWN;
                        }
                    }

                    SmallRyeHealth smallRyeHealth = result.toSmallRyeHealth();
                    fireGlobalHealthStatusChangeIfNeeded(smallRyeHealth,
                            List.of(livenessStatus, readinessStatus, startupStatus, wellnessStatus));

                    return smallRyeHealth;
                });
    }

    private HealthCheckResponse.Status handleHealthResult(HealthResult partialResult, HealthType healthType,
            Event<SmallRyeHealthStatusChangeEvent> event,
            HealthCheckResponse.Status currentStatus, HealthResult result) {
        if (!partialResult.checks.isEmpty()) {
            result.checks.addAll(partialResult.checks);

            if (result.status == UP && partialResult.status == DOWN) {
                result.status = DOWN;
            }

            return fireEventIfStatusChanged(event, healthType, currentStatus, partialResult);
        }
        return currentStatus;
    }

    private HealthCheckResponse.Status fireEventIfStatusChanged(Event<SmallRyeHealthStatusChangeEvent> event,
            HealthType healthType,
            HealthCheckResponse.Status oldStatus, HealthResult result) {
        HealthCheckResponse.Status newStatus = result.status;
        if (event == null) {
            return newStatus;
        }
        if (oldStatus != newStatus) {
            try {
                event.fire(new SmallRyeHealthStatusChangeEvent(healthType, result.toSmallRyeHealth()));
            } catch (ObserverException e) {
                HealthLogging.logger.healthChangeObserverError(e);
            }
        }

        return newStatus;
    }

    private void fireGlobalHealthStatusChangeIfNeeded(SmallRyeHealth smallRyeHealth,
            List<HealthCheckResponse.Status> healthStatuses) {
        if (healthEvent == null) {
            return;
        }

        HealthCheckResponse.Status newStatus = UP;
        if (healthStatuses.contains(DOWN)) {
            newStatus = DOWN;
        }

        if (healthStatus != newStatus) {
            healthStatus = newStatus;
            try {
                healthEvent.fire(new SmallRyeHealthStatusChangeEvent(smallRyeHealth));
            } catch (ObserverException e) {
                HealthLogging.logger.healthChangeObserverError(e);
            }
        }

    }

    @SafeVarargs
    private Uni<HealthResult> getHealthResult(Collection<Uni<HealthCheckResponse>>... checkLists) {
        List<Uni<HealthCheckResponse>> healthCheckUnis = new ArrayList<>();

        if (checkLists != null) {
            for (Collection<Uni<HealthCheckResponse>> checks : checkLists) {
                healthCheckUnis.addAll(checks);
            }
        }

        if (healthCheckUnis.isEmpty()) {
            return emptyHealthResult();
        }

        // Need to use Uni.join() because Uni.combine() has a performance issue - https://github.com/smallrye/smallrye-mutiny/issues/1993
        return Uni.join().all(healthCheckUnis).andCollectFailures()
                .map(healthCheckResponses -> {
                    HealthResult healthResult = new HealthResult();

                    for (HealthCheckResponse response : healthCheckResponses) {
                        if (healthResult.status == UP && response.getStatus() == DOWN) {
                            healthResult.status = DOWN;
                        }

                        healthResult.checks.add(response);
                    }

                    return healthResult;
                });
    }

    private boolean isHealthCheckEnabled(HealthCheck healthCheck) {
        return isEnabled(healthCheck.getClass().getName()) && isEnabled(healthCheck.getClass().getSuperclass().getName());
    }

    private boolean isHealthCheckEnabled(AsyncHealthCheck asyncHealthCheck) {
        return isEnabled(asyncHealthCheck.getClass().getName())
                && isEnabled(asyncHealthCheck.getClass().getSuperclass().getName());
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

    private final class HealthResult {
        HealthCheckResponse.Status status = UP;
        List<HealthCheckResponse> checks = new ArrayList<>();

        public HealthResult() {
        }

        public HealthResult(HealthCheckResponse.Status status) {
            this.status = status;
        }

        public SmallRyeHealth toSmallRyeHealth() {
            return createSmallRyeHealth();
        }

        private SmallRyeHealth createSmallRyeHealth() {
            JsonObjectBuilder builder = JSON_PROVIDER.createObjectBuilder();
            JsonArray checkResults = createChecksJsonArray();

            builder.add("status", checkResults.isEmpty() ? emptyChecksOutcome.toString() : status.toString());
            builder.add("checks", checkResults);

            if (!additionalProperties.isEmpty()) {
                additionalProperties.forEach(builder::add);
            }

            return new SmallRyeHealth(builder.build());
        }

        private JsonArray createChecksJsonArray() {
            JsonArrayBuilder results = JSON_PROVIDER.createArrayBuilder();

            for (HealthCheckResponse response : checks) {
                handleResponse(response, results);
            }

            return results.build();
        }

        private void handleResponse(HealthCheckResponse response, JsonArrayBuilder results) {
            JsonObject responseJson = jsonObject(response);
            results.add(responseJson);
        }

        private JsonObject jsonObject(HealthCheckResponse response) {
            JsonObjectBuilder builder = JSON_PROVIDER.createObjectBuilder();
            builder.add("name", response.getName());
            builder.add("status", response.getStatus().toString());
            response.getData().ifPresent(d -> {
                JsonObjectBuilder data = JSON_PROVIDER.createObjectBuilder();
                for (Map.Entry<String, Object> entry : d.entrySet()) {
                    Object value = entry.getValue();
                    if (value instanceof String s) {
                        data.add(entry.getKey(), s);
                    } else if (value instanceof Long l) {
                        data.add(entry.getKey(), l);
                    } else if (value instanceof Boolean b) {
                        data.add(entry.getKey(), b);
                    }
                }
                builder.add("data", data.build());
            });

            return builder.build();
        }
    }
}
