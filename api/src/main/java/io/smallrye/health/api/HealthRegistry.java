/*
 * Copyright (c) 2020 Contributors to the Eclipse Foundation
 *
 * See the NOTICES file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 *
 */
package io.smallrye.health.api;

import org.eclipse.microprofile.health.HealthCheck;

import io.smallrye.common.annotation.Experimental;

/**
 * <p>
 * Programmatic API for the dynamic registrations and removals of health check procedures.
 * </p>
 * 
 * <p>
 * The {@link HealthRegistry} can be injected as a CDI bean with two qualifiers:
 * </p>
 * 
 * <ul>
 * <li><b>Liveness</b></li>
 * </ul>
 * 
 * <pre>
 * &#64;Inject
 * &#64;Liveness
 * HealthRegistry livenessHealthRegistry;
 * </pre>
 * 
 * <ul>
 * <li><b>Readiness</b>:</li>
 * </ul>
 * 
 * <pre>
 * &#64;Inject
 * &#64;Readiness
 * HealthRegistry readinessHealthRegistry;
 * </pre>
 */
@Experimental("Programmatic Health API")
public interface HealthRegistry {

    /**
     * Programmatic registration of a {@link HealthCheck} instances.
     * 
     * @param id the id of the registered check which can be later used for its removal
     * @param healthCheck the {@link HealthCheck} instance to be registered
     * @return this instance for fluent registration
     * @throws IllegalStateException if the {@link HealthCheck} instance cannot be registered
     */
    HealthRegistry register(String id, HealthCheck healthCheck);

    /**
     * Asynchronous variant of the {@link HealthRegistry#register(String, HealthCheck)}.
     * 
     * @param id the id of the registered asynchronous check which can be later used for its removal
     * @param asyncHealthCheck the {@link AsyncHealthCheck} instance to be registered
     * @return this instance for fluent registration
     * @throws IllegalStateException if the {@link HealthCheck} instance cannot be registered
     */
    HealthRegistry register(String id, AsyncHealthCheck asyncHealthCheck);

    /**
     * Programmatic registration of a {@link HealthCheck} intances with the id set to
     * to the health check class name.
     * 
     * @param healthCheck the {@link HealthCheck} instance to be registered
     * @return this instance for fluent registration
     * @throws IllegalStateException if the {@link HealthCheck} instance cannot be registered
     */
    default HealthRegistry register(HealthCheck healthCheck) {
        register(healthCheck.getClass().getName(), healthCheck);
        return this;
    }

    /**
     * Asynchronous variant of {@link HealthRegistry#register(HealthCheck)}.
     * 
     * @param asyncHealthCheck the {@link AsyncHealthCheck} instance to be registered
     * @return this instance for fluent registration
     * @throws IllegalStateException if the {@link HealthCheck} instance cannot be registered
     */
    default HealthRegistry register(AsyncHealthCheck asyncHealthCheck) {
        register(asyncHealthCheck.getClass().getName(), asyncHealthCheck);
        return this;
    }

    /**
     * Programmatic removal of a programmatically registered check
     * ({@link HealthCheck} or {@link AsyncHealthCheck}) instances.
     * 
     * @param id the id of the registered check to be removed
     * @return this instance for fluent registration
     * @throws IllegalStateException if the {@link HealthCheck} instance cannot be removed
     */
    HealthRegistry remove(String id);

    /**
     * Programmatic removal of a programmatically registered {@link HealthCheck} instances with the id set to
     * the the health check class name.
     * 
     * @param healthCheck the {@link HealthCheck} instance to be removed
     * @return this instance for fluent registration
     * @throws IllegalStateException if the {@link HealthCheck} instance cannot be removed
     */
    default HealthRegistry remove(HealthCheck healthCheck) {
        remove(healthCheck.getClass().getName());
        return this;
    }

    /**
     * Asynchronous variant of {@link HealthRegistry#remove(HealthCheck)}.
     * 
     * @param asyncHealthCheck the {@link AsyncHealthCheck} instance to be removed
     * @return this instance for fluent registration
     * @throws IllegalStateException if the {@link HealthCheck} instance cannot be removed
     */
    default HealthRegistry remove(AsyncHealthCheck asyncHealthCheck) {
        remove(asyncHealthCheck.getClass().getName());
        return this;
    }
}
