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

import jakarta.json.JsonArray;
import jakarta.json.JsonObject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.Archive;
import org.testng.Assert;
import org.testng.annotations.Test;

import io.smallrye.health.deployment.AsyncDependentHealthCheck;
import io.smallrye.health.deployment.DependentCallRecorder;
import io.smallrye.health.deployment.DependentHealthCheck;

public class DependentHealthChecksTest extends TCKBase {

    @Deployment
    public static Archive getDeployment() {
        return DeploymentUtils.createWarFileWithClasses(DependentHealthChecksTest.class.getSimpleName(),
                DependentHealthCheck.class, AsyncDependentHealthCheck.class, DependentCallRecorder.class, TCKBase.class);
    }

    @Test
    public void testAsyncLiveness() {
        Response response = getUrlLiveContents();

        // status code
        Assert.assertEquals(response.getStatus(), 200);

        JsonObject json = readJson(response);

        // response size
        JsonArray checks = json.getJsonArray("checks");
        Assert.assertEquals(checks.size(), 2, "Expected two check responses");

        for (JsonObject check : checks.getValuesAs(JsonObject.class)) {
            String id = check.getString("name");

            if (id.equals(DependentHealthCheck.class.getName())) {
                verifySuccessStatus(check);
                Assert.assertTrue(DependentCallRecorder.healthCheckPreDestroyCalled,
                        "HealthCheck - PreDestroy method was not called");
            } else if (id.equals(AsyncDependentHealthCheck.class.getName())) {
                verifySuccessStatus(check);
                Assert.assertTrue(DependentCallRecorder.asyncHealthCheckPreDestroyCalled,
                        "AsyncHealthCheck - PreDestroy method was not called");
            } else {
                Assert.fail("Unexpected response payload structure");
            }
        }

        assertOverallSuccess(json);
    }
}
