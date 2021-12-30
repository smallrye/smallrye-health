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

import java.util.function.Supplier;

import jakarta.json.JsonArray;
import jakarta.json.JsonObject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.shrinkwrap.api.Archive;
import org.testng.Assert;
import org.testng.annotations.Test;

import io.smallrye.health.deployment.ChangingLivenessHealthCheck;
import io.smallrye.health.deployment.ChangingReadinessHealthCheckAsync;

public class ChangingHealthTest extends TCKBase {

    @Deployment
    public static Archive getDeployment() {
        return DeploymentUtils.createWarFileWithClasses(ChangingHealthTest.class.getSimpleName(),
                ChangingLivenessHealthCheck.class, ChangingReadinessHealthCheckAsync.class, TCKBase.class);
    }

    @Test
    @RunAsClient
    public void testChangingHealthCheck() {
        testChangingHealth(this::getUrlLiveContents);
    }

    @Test
    @RunAsClient
    public void testChangingAsyncHealthCheck() {
        testChangingHealth(this::getUrlReadyContents);
    }

    public void testChangingHealth(Supplier<Response> supplier) {
        // first invocation -> success
        Response response = supplier.get();

        // status code
        Assert.assertEquals(response.getStatus(), 200);

        JsonObject json = readJson(response);

        // response size
        JsonArray checks = json.getJsonArray("checks");
        Assert.assertEquals(checks.size(), 1, "Expected one check response");

        JsonObject check = checks.getJsonObject(0);
        Assert.assertEquals(check.getString("name"), "up");
        verifySuccessStatus(check);

        assertOverallSuccess(json);

        // second invocation -> failure
        response = supplier.get();

        // status code
        Assert.assertEquals(response.getStatus(), 503);

        json = readJson(response);

        // response size
        checks = json.getJsonArray("checks");
        Assert.assertEquals(checks.size(), 1, "Expected one check response");

        check = checks.getJsonObject(0);
        Assert.assertEquals(check.getString("name"), "down");
        verifyFailureStatus(check);

        assertOverallFailure(json);
    }

}
