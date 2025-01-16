/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation
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

package io.smallrye.health.test;

import static org.eclipse.microprofile.health.HealthCheckResponse.Status.DOWN;
import static org.eclipse.microprofile.health.HealthCheckResponse.Status.UP;
import static org.testng.Assert.assertEquals;

import java.util.function.Supplier;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;

import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.HealthCheckResponseBuilder;
import org.eclipse.microprofile.health.Liveness;
import org.eclipse.microprofile.health.Readiness;
import org.eclipse.microprofile.health.Startup;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.shrinkwrap.api.Archive;
import org.testng.annotations.Test;

import io.smallrye.health.api.Wellness;
import io.smallrye.health.deployment.TestHealthObserver;

public class HealthObserversTest extends TCKBase {

    @Inject
    TestHealthObserver testHealthObserver;

    private static final String LIVENESS_CHANGING = "liveness-changing";

    @Liveness
    @ApplicationScoped
    private static final class LivenessChanging implements HealthCheck {

        private int counter = 0;

        @Override
        public HealthCheckResponse call() {
            HealthCheckResponseBuilder response = HealthCheckResponse.named(LivenessChanging.class.getName());
            response = counter++ % 2 == 0 ? response.up() : response.down();
            return response.build();
        }
    }

    @Deployment(name = LIVENESS_CHANGING)
    public static Archive livenessChangingDeployment() {
        return DeploymentUtils.createWarFileWithClasses(LivenessChanging.class.getSimpleName(),
                TestHealthObserver.class, LivenessChanging.class, TCKBase.class);
    }

    @Test
    @OperateOnDeployment(LIVENESS_CHANGING)
    public void testLivenessObserver() {
        verifyLivenessWithEvent(UP, 0, LivenessChanging.class.getName());
        verifyLivenessWithEvent(DOWN, 1, LivenessChanging.class.getName());
        verifyLivenessWithEvent(UP, 2, LivenessChanging.class.getName());
    }

    private static final String READINESS_CHANGING = "readiness-changing";

    @Readiness
    @ApplicationScoped
    private static final class ReadinessChanging implements HealthCheck {

        private int counter = 0;

        @Override
        public HealthCheckResponse call() {
            HealthCheckResponseBuilder response = HealthCheckResponse.named(ReadinessChanging.class.getName());
            response = counter++ % 2 == 0 ? response.up() : response.down();
            return response.build();
        }
    }

    @Deployment(name = READINESS_CHANGING)
    public static Archive readinessChangingDeployment() {
        return DeploymentUtils.createWarFileWithClasses(ReadinessChanging.class.getSimpleName(),
                TestHealthObserver.class, ReadinessChanging.class, TCKBase.class);
    }

    @Test
    @OperateOnDeployment(READINESS_CHANGING)
    public void testReadinessObserver() {
        verifyReadinessWithEvent(UP, 0, ReadinessChanging.class.getName());
        verifyReadinessWithEvent(DOWN, 1, ReadinessChanging.class.getName());
        verifyReadinessWithEvent(UP, 2, ReadinessChanging.class.getName());
    }

    private static final String WELLNESS_CHANGING = "wellness-changing";

    @Wellness
    @ApplicationScoped
    private static final class WellnessChanging implements HealthCheck {

        private int counter = 0;

        @Override
        public HealthCheckResponse call() {
            HealthCheckResponseBuilder response = HealthCheckResponse.named(WellnessChanging.class.getName());
            response = counter++ % 2 == 0 ? response.up() : response.down();
            return response.build();
        }
    }

    @Deployment(name = WELLNESS_CHANGING)
    public static Archive wellnessChangingDeployment() {
        return DeploymentUtils.createWarFileWithClasses(WellnessChanging.class.getSimpleName(),
                TestHealthObserver.class, WellnessChanging.class, TCKBase.class);
    }

    @Test
    @OperateOnDeployment(WELLNESS_CHANGING)
    public void testWellnessObserver() {
        verifyWellnessWithEvent(UP, 0, WellnessChanging.class.getName());
        verifyWellnessWithEvent(DOWN, 1, WellnessChanging.class.getName());
        verifyWellnessWithEvent(UP, 2, WellnessChanging.class.getName());
    }

    private static final String STARTUP_CHANGING = "startup-changing";

    @Startup
    @ApplicationScoped
    private static final class StartupChanging implements HealthCheck {

        private int counter = 0;

        @Override
        public HealthCheckResponse call() {
            HealthCheckResponseBuilder response = HealthCheckResponse.named(StartupChanging.class.getName());
            response = counter++ % 2 == 0 ? response.up() : response.down();
            return response.build();
        }
    }

    @Deployment(name = STARTUP_CHANGING)
    public static Archive startupChangingDeployment() {
        return DeploymentUtils.createWarFileWithClasses(StartupChanging.class.getSimpleName(),
                TestHealthObserver.class, StartupChanging.class, TCKBase.class);
    }

    @Test
    @OperateOnDeployment(STARTUP_CHANGING)
    public void testStartupObserver() {
        verifyStartupWithEvent(UP, 0, StartupChanging.class.getName());
        verifyStartupWithEvent(DOWN, 1, StartupChanging.class.getName());
        verifyStartupWithEvent(UP, 2, StartupChanging.class.getName());
    }

    private static final String HEALTH_CHANGING = "health-changing";

    // Global health contains all previous checks which all behave the same
    @Deployment(name = HEALTH_CHANGING)
    public static Archive healthChangingDeployment() {
        return DeploymentUtils.createWarFileWithClasses(HealthObserversTest.class.getSimpleName(),
                TestHealthObserver.class, TCKBase.class);
    }

    @Test
    @OperateOnDeployment(HEALTH_CHANGING)
    public void testHealthObserver() {
        verifyHealthWithEvent(UP, 0);
        verifyHealthWithEvent(DOWN, 1);
        verifyHealthWithEvent(UP, 2);
    }

    private void verifyLivenessWithEvent(HealthCheckResponse.Status status, int numberOfEvents, String checkName) {
        verifyWithEvent(status, numberOfEvents, checkName, this::getUrlLiveContents,
                () -> testHealthObserver.getCounterLiveness(),
                () -> testHealthObserver.getLivenessStatus(), "liveness");
    }

    private void verifyReadinessWithEvent(HealthCheckResponse.Status status, int numberOfEvents, String checkName) {
        verifyWithEvent(status, numberOfEvents, checkName, this::getUrlReadyContents,
                () -> testHealthObserver.getCounterReadiness(),
                () -> testHealthObserver.getReadinessStatus(), "readiness");
    }

    private void verifyWellnessWithEvent(HealthCheckResponse.Status status, int numberOfEvents, String checkName) {
        verifyWithEvent(status, numberOfEvents, checkName, this::getUrlWellContents,
                () -> testHealthObserver.getCounterWellness(),
                () -> testHealthObserver.getWellnessStatus(), "wellness");
    }

    private void verifyStartupWithEvent(HealthCheckResponse.Status status, int numberOfEvents, String checkName) {
        verifyWithEvent(status, numberOfEvents, checkName, this::getUrlStartedContents,
                () -> testHealthObserver.getCounterStartup(),
                () -> testHealthObserver.getStartupStatus(), "startup");
    }

    private void verifyWithEvent(HealthCheckResponse.Status status, int numberOfEvents, String checkName,
            Supplier<Response> responseSupplier,
            Supplier<Integer> counterSupplier, Supplier<HealthCheckResponse.Status> statusSupplier, String healthType) {
        Response response = responseSupplier.get();

        // status code
        assertEquals(response.getStatus(), status == UP ? 200 : 503);

        JsonObject json = readJson(response);

        // response size
        JsonArray checks = json.getJsonArray("checks");
        assertEquals(checks.size(), 1, "Expected one check response");

        // single procedure response
        if (status == UP) {
            assertSuccessfulCheck(checks.getJsonObject(0), checkName);
            assertOverallSuccess(json);
        } else {
            assertFailureCheck(checks.getJsonObject(0), checkName);
            assertOverallFailure(json);
        }

        assertEquals(counterSupplier.get(), numberOfEvents,
                String.format("After this call, the %s event should have been fired %d times", healthType, numberOfEvents));
        assertEquals(statusSupplier.get(), status,
                String.format("After this call, the %s status should be %s", healthType, status));
    }

    private void verifyHealthWithEvent(HealthCheckResponse.Status status, int numberOfEvents) {
        Response response = getUrlHealthContents();

        // status code
        assertEquals(response.getStatus(), status == UP ? 200 : 503);

        JsonObject json = readJson(response);

        // response size
        JsonArray checks = json.getJsonArray("checks");
        assertEquals(checks.size(), 4, "Expected four check responses");

        // single procedure response
        if (status == UP) {
            for (int i = 0; i < 4; i++) {
                verifySuccessStatus(checks.getJsonObject(i));
            }
        } else {
            for (int i = 0; i < 4; i++) {
                verifyFailureStatus(checks.getJsonObject(i));
            }
        }

        assertEquals(testHealthObserver.getCounterHealth(), numberOfEvents,
                String.format("After this call, the global health event should have been fired %d times", numberOfEvents));
        assertEquals(testHealthObserver.getHealthStatus(), status,
                String.format("After this call, the global health status should be %s", status));
    }
}
