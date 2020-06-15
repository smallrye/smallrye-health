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

package io.smallrye.health.test;

import java.time.Duration;

import javax.inject.Inject;
import javax.json.JsonArray;
import javax.json.JsonObject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.Archive;
import org.testng.Assert;
import org.testng.annotations.Test;

import io.smallrye.health.SmallRyeHealthReporter;
import io.smallrye.health.deployment.FailedLivenessAsync;
import io.smallrye.health.deployment.FailedReadinessAsync;

public class AsyncFailedHealthTest extends TCKBase {

    @Inject
    SmallRyeHealthReporter smallRyeHealthReporter;

    @Deployment
    public static Archive getDeployment() {
        return DeploymentUtils.createWarFileWithClasses(AsyncFailedHealthTest.class.getSimpleName(),
                FailedLivenessAsync.class, FailedReadinessAsync.class, TCKBase.class);
    }

    @Test
    public void testAsyncLiveness() {
        JsonObject json = smallRyeHealthReporter.getLivenessAsync().await().atMost(Duration.ofSeconds(5)).getPayload();

        // response size
        JsonArray checks = json.getJsonArray("checks");
        Assert.assertEquals(checks.size(), 1, "Expected one check response");

        JsonObject checkJson = checks.getJsonObject(0);
        Assert.assertEquals(FailedLivenessAsync.class.getName(), checkJson.getString("name"));
        verifyFailureStatus(checkJson);

        assertOverallFailure(json);
    }

    @Test
    public void testAsyncReadiness() {
        JsonObject json = smallRyeHealthReporter.getReadinessAsync().await().atMost(Duration.ofSeconds(5)).getPayload();

        // response size
        JsonArray checks = json.getJsonArray("checks");
        Assert.assertEquals(checks.size(), 1, "Expected one check response");

        JsonObject checkJson = checks.getJsonObject(0);
        Assert.assertEquals(FailedReadinessAsync.class.getName(), checkJson.getString("name"));
        verifyFailureStatus(checkJson);

        assertOverallFailure(json);
    }

}
