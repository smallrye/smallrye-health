package io.smallrye.health.api;

import org.eclipse.microprofile.health.HealthCheckResponse;

import io.smallrye.common.annotation.Experimental;
import io.smallrye.mutiny.Uni;

/**
 * The async health check procedure interface.
 * Invoked by consumers to verify the healthiness of a computing node in an asynchronous manner.
 * Unhealthy nodes are expected to be terminated.
 */
@FunctionalInterface
@Experimental("Asynchronous Health Check procedures")
public interface AsyncHealthCheck {

    /**
     * Invokes the health check procedure provided by the implementation of this interface.
     * Unlike synchronous checks, this method is used for asynchronous checks. The returned {@code Uni}
     * propagates the {@code HealthCheckResponse} as item. If the returned {@code Uni} produces
     * a failure, the check is considered as failed. Returning {@code null} is invalid and considers the
     * check failed. In addition, returning a {@code Uni} propagating a {@code null} value as the item
     * is considered also as a failure.
     *
     * @return {@link Uni<HealthCheckResponse>} object containing information about the health check result
     */
    Uni<HealthCheckResponse> call();
}
