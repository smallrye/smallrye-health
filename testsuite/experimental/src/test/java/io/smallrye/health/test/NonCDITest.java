/*
 * Copyright (c) 2025 Contributors to the Eclipse Foundation
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

import java.util.List;

import jakarta.json.JsonArray;
import jakarta.json.JsonObject;

import org.eclipse.microprofile.health.HealthCheckResponse;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.Archive;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import io.smallrye.health.SmallRyeHealth;
import io.smallrye.health.SmallRyeHealthReporter;
import io.smallrye.health.api.HealthRegistry;
import io.smallrye.health.api.HealthType;
import io.smallrye.health.deployment.FailedLiveness;
import io.smallrye.health.deployment.FailedLivenessAsync;
import io.smallrye.health.deployment.SuccessLiveness;
import io.smallrye.health.deployment.SuccessLivenessAsync;
import io.smallrye.health.deployment.SuccessReadiness;
import io.smallrye.health.deployment.SuccessStartup;
import io.smallrye.health.deployment.SuccessWellness;
import io.smallrye.health.registry.HealthRegistries;

public class NonCDITest extends TCKBase {

    private SmallRyeHealthReporter reporter;

    @Deployment
    public static Archive getDeployment() {
        return DeploymentUtils.createWarFileWithClasses(NonCDITest.class.getSimpleName(),
                SuccessLiveness.class, SuccessLivenessAsync.class, FailedLiveness.class, FailedLivenessAsync.class,
                SuccessReadiness.class, SuccessStartup.class, SuccessWellness.class,
                TCKBase.class);
    }

    @BeforeTest
    public void beforeTest() {
        reporter = new SmallRyeHealthReporter();
    }

    @Test
    public void testNonCDIHealthSuccess() {
        reporter.addHealthCheck(new SuccessLiveness());
        reporter.addHealthCheck(new SuccessLivenessAsync());

        SmallRyeHealth result = reporter.getHealth();

        Assert.assertEquals(result.getStatus(), HealthCheckResponse.Status.UP);

        JsonObject json = result.getPayload();

        // response size
        JsonArray checks = json.getJsonArray("checks");
        Assert.assertEquals(checks.size(), 2, "Expected two checks in the response");
        List<String> expectedNames = List.of(SuccessLiveness.class.getName(), SuccessLivenessAsync.class.getName());

        for (JsonObject check : checks.getValuesAs(JsonObject.class)) {
            String id = check.getString("name");
            if (expectedNames.contains(id)) {
                verifySuccessStatus(check);
            } else {
                Assert.fail("Unexpected check name " + id);
            }
        }

        assertOverallSuccess(json);
    }

    @Test
    public void testNonCDIHealthFailed() {
        reporter.addHealthCheck(new FailedLiveness());
        reporter.addHealthCheck(new FailedLivenessAsync());

        SmallRyeHealth result = reporter.getHealth();

        Assert.assertEquals(result.getStatus(), HealthCheckResponse.Status.DOWN);

        JsonObject json = result.getPayload();

        // response size
        JsonArray checks = json.getJsonArray("checks");
        Assert.assertEquals(checks.size(), 2, "Expected two checks in the response");
        List<String> expectedNames = List.of(FailedLiveness.class.getName(), FailedLivenessAsync.class.getName());

        for (JsonObject check : checks.getValuesAs(JsonObject.class)) {
            String id = check.getString("name");
            if (expectedNames.contains(id)) {
                verifyFailureStatus(check);
            } else {
                Assert.fail("Unexpected check name " + id);
            }
        }

        assertOverallFailure(json);
    }

    @Test
    public void testNonCDIHealthMixed() {
        reporter.addHealthCheck(new SuccessLiveness());
        reporter.addHealthCheck(new SuccessLivenessAsync());
        reporter.addHealthCheck(new FailedLiveness());
        reporter.addHealthCheck(new FailedLivenessAsync());

        SmallRyeHealth result = reporter.getHealth();

        Assert.assertEquals(result.getStatus(), HealthCheckResponse.Status.DOWN);

        JsonObject json = result.getPayload();

        // response size
        JsonArray checks = json.getJsonArray("checks");
        Assert.assertEquals(checks.size(), 4, "Expected four checks in the response");
        List<String> expectedNames = List.of(SuccessLiveness.class.getName(), SuccessLivenessAsync.class.getName(),
                FailedLiveness.class.getName(), FailedLivenessAsync.class.getName());

        for (JsonObject check : checks.getValuesAs(JsonObject.class)) {
            String id = check.getString("name");
            if (expectedNames.contains(id)) {
                if (id.contains("Success")) {
                    verifySuccessStatus(check);
                } else {
                    verifyFailureStatus(check);
                }
            } else {
                Assert.fail("Unexpected check name " + id);
            }
        }

        assertOverallFailure(json);
    }

    @Test
    public void testRemovingHealthChecks() {
        SuccessLiveness check = new SuccessLiveness();
        reporter.addHealthCheck(check);

        SmallRyeHealth result = reporter.getHealth();
        Assert.assertEquals(result.getStatus(), HealthCheckResponse.Status.UP);
        Assert.assertEquals(result.getPayload().getJsonArray("checks").size(), 1, "Expected one check in the response");
        JsonObject checkJson = result.getPayload().getJsonArray("checks").getJsonObject(0);
        Assert.assertEquals(checkJson.getString("name"), SuccessLiveness.class.getName());
        verifySuccessStatus(checkJson);

        reporter.removeHealthCheck(check);
        result = reporter.getHealth();
        Assert.assertEquals(result.getStatus(), HealthCheckResponse.Status.UP);
        Assert.assertEquals(result.getPayload().getJsonArray("checks").size(), 0, "Expected no checks in the response");
    }

    @Test
    public void testHealthRegistriesWithoutCDI() {
        HealthRegistry livenessRegistry = HealthRegistries.getRegistry(HealthType.LIVENESS);
        Assert.assertNotNull(livenessRegistry);
        livenessRegistry.register("live-check", new SuccessLiveness());

        HealthRegistry readinessRegistry = HealthRegistries.getRegistry(HealthType.READINESS);
        Assert.assertNotNull(readinessRegistry);
        readinessRegistry.register("ready-check", new SuccessReadiness());

        HealthRegistry startupRegistry = HealthRegistries.getRegistry(HealthType.STARTUP);
        Assert.assertNotNull(startupRegistry);
        startupRegistry.register("start-check", new SuccessStartup());

        HealthRegistry wellnessRegistry = HealthRegistries.getRegistry(HealthType.WELLNESS);
        Assert.assertNotNull(wellnessRegistry);
        wellnessRegistry.register("well-check", new SuccessWellness());

        SmallRyeHealth result = reporter.getHealth();
        Assert.assertEquals(result.getStatus(), HealthCheckResponse.Status.UP);

        JsonObject json = result.getPayload();

        // response size
        JsonArray checks = json.getJsonArray("checks");
        Assert.assertEquals(checks.size(), 4, "Expected four checks in the response");
        List<String> expectedNames = List.of(SuccessLiveness.class.getName(), SuccessReadiness.class.getName(),
                SuccessStartup.class.getName(), SuccessWellness.class.getName());

        for (JsonObject check : checks.getValuesAs(JsonObject.class)) {
            String id = check.getString("name");
            if (expectedNames.contains(id)) {
                verifySuccessStatus(check);
            } else {
                Assert.fail("Unexpected check name " + id);
            }
        }

        assertOverallSuccess(json);

        livenessRegistry.remove("live-check");
        readinessRegistry.remove("ready-check");
        startupRegistry.remove("start-check");
        wellnessRegistry.remove("well-check");

        result = reporter.getHealth();
        Assert.assertEquals(result.getStatus(), HealthCheckResponse.Status.UP);
        json = result.getPayload();
        checks = json.getJsonArray("checks");
        Assert.assertEquals(checks.size(), 0, "Expected no checks in the response");
        assertOverallSuccess(json);
    }
}
