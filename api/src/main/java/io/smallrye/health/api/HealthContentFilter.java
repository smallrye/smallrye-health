package io.smallrye.health.api;

import jakarta.json.JsonObject;

import io.smallrye.common.annotation.Experimental;

/**
 * Implementations of this interface are invoked prior the health report to filter(remove, add, or modify) fields in the
 * returned JSON object. Individual implementations are collected through CDI so they must be annotated with some of the
 * bean-defining annotations (e.g., {@link jakarta.enterprise.context.ApplicationScoped}).
 */
@FunctionalInterface
@Experimental("Health content filtering")
public interface HealthContentFilter {

    /**
     * Filters (removes, adds, or modifies) fields in the provided {@link JsonObject}.
     *
     * @param payload An object representing the payload to be reported to the caller.
     */
    JsonObject filter(JsonObject payload);
}
