/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
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

import java.util.Arrays;

import jakarta.inject.Inject;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;

import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Liveness;
import org.eclipse.microprofile.health.Readiness;
import org.eclipse.microprofile.health.Startup;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.Archive;
import org.testng.Assert;
import org.testng.annotations.Ignore;
import org.testng.annotations.Test;

import io.smallrye.health.SmallRyeHealth;
import io.smallrye.health.SmallRyeHealthReporter;
import io.smallrye.health.api.HealthRegistry;
import io.smallrye.health.api.Wellness;

/**
 * Simple sanity check that injected registries work in CDI environment. The registries' functionality tests are
 * available in smallrye-health module unit tests.
 */
public class HealthRegistriesInjectionTest extends TCKBase {

    @Inject
    @Liveness
    HealthRegistry livenessHealthRegistry;

    @Inject
    @Readiness
    HealthRegistry readinessHealthRegistry;

    @Inject
    @Startup
    HealthRegistry startupHealthRegistry;

    @Inject
    @Wellness
    HealthRegistry wellnessHealthRegistry;

    @Inject
    SmallRyeHealthReporter reporter;

    @Deployment
    public static Archive getDeployment() {
        return DeploymentUtils.createWarFileWithClasses(HealthRegistriesInjectionTest.class.getSimpleName(),
                TCKBase.class);
    }

    @Test
    @Ignore
    public void livenessRegistryInjectionTest() {
        HealthCheck livenessCheck = () -> HealthCheckResponse.up("live");

        livenessHealthRegistry.register("l1", livenessCheck);

        assertContainsChecks(reporter.getLiveness(), "live");
        assertContainsChecks(reporter.getHealth(), "live");

        livenessHealthRegistry.remove("l1");

        assertEmpty(reporter.getLiveness());
        assertEmpty(reporter.getHealth());
    }

    @Test
    @Ignore
    public void readinessRegistryInjectionTest() {
        HealthCheck readinessCheck = () -> HealthCheckResponse.up("ready");

        readinessHealthRegistry.register("r1", readinessCheck);

        assertContainsChecks(reporter.getReadiness(), "ready");
        assertContainsChecks(reporter.getHealth(), "ready");

        readinessHealthRegistry.remove("r1");

        assertEmpty(reporter.getReadiness());
        assertEmpty(reporter.getHealth());
    }

    @Test
    @Ignore
    public void startupRegistryInjectionTest() {
        HealthCheck startupCheck = () -> HealthCheckResponse.up("started");

        startupHealthRegistry.register("s1", startupCheck);

        assertContainsChecks(reporter.getStartup(), "started");
        assertContainsChecks(reporter.getHealth(), "started");

        startupHealthRegistry.remove("s1");

        assertEmpty(reporter.getStartup());
        assertEmpty(reporter.getHealth());
    }

    @Test
    @Ignore
    public void wellnessRegistryInjectionTest() {
        HealthCheck wellnessCheck = () -> HealthCheckResponse.up("wellness");

        wellnessHealthRegistry.register("w1", wellnessCheck);

        assertContainsChecks(reporter.getWellness(), "wellness");
        assertContainsChecks(reporter.getHealth(), "wellness");

        wellnessHealthRegistry.remove("w1");

        assertEmpty(reporter.getWellness());
        assertEmpty(reporter.getHealth());
    }

    private void assertContainsChecks(SmallRyeHealth health, String... checkNames) {
        JsonArray checks = health.getPayload().getJsonArray("checks");
        Assert.assertEquals(checkNames.length, checks.size());
        for (JsonObject check : checks.getValuesAs(JsonObject.class)) {
            Assert.assertTrue(Arrays.stream(checkNames)
                    .anyMatch(s -> s.equals(check.getString("name"))));
        }
    }

    private void assertEmpty(SmallRyeHealth health) {
        Assert.assertEquals(0, health.getPayload().getJsonArray("checks").size());
    }
}
