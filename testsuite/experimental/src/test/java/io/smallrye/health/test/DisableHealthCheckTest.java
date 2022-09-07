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

import jakarta.json.JsonArray;
import jakarta.json.JsonObject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.testng.Assert;
import org.testng.annotations.Test;

import io.smallrye.health.deployment.SuccessLiveness;
import io.smallrye.health.deployment.SuccessLivenessAsync;
import io.smallrye.health.deployment.SuccessReadiness;
import io.smallrye.health.deployment.SuccessReadinessAsync;

@RunAsClient
public class DisableHealthCheckTest extends TCKBase {

    private static final String SYNC_DEPLOYMENT = "syncDeployment";
    private static final String ASYNC_DEPLOYMENT = "asyncDeployment";

    @Deployment(name = SYNC_DEPLOYMENT)
    public static Archive getSyncDeployment() {
        return DeploymentUtils.createWarFileWithClasses(DisableHealthCheckTest.class.getSimpleName() + "-sync",
                SuccessLiveness.class, SuccessReadiness.class, TCKBase.class)
                .addAsManifestResource(
                        new StringAsset("io.smallrye.health.check." + SuccessLiveness.class.getName() + ".enabled=false"),
                        "microprofile-config.properties");
    }

    @Deployment(name = ASYNC_DEPLOYMENT)
    public static Archive getAsyncDeployment() {
        return DeploymentUtils.createWarFileWithClasses(DisableHealthCheckTest.class.getSimpleName() + "-async",
                SuccessLivenessAsync.class, SuccessReadinessAsync.class, TCKBase.class)
                .addAsManifestResource(
                        new StringAsset("io.smallrye.health.check." + SuccessLivenessAsync.class.getName() + ".enabled=false"),
                        "microprofile-config.properties");
    }

    @Test
    @OperateOnDeployment(SYNC_DEPLOYMENT)
    public void testAdditionalProperties() {
        Response response = getUrlHealthContents();

        Assert.assertEquals(response.getStatus(), 200);

        JsonObject json = readJson(response);

        // response size, SuccessLiveness is in the deployment but it should not be included in the response
        JsonArray checks = json.getJsonArray("checks");
        Assert.assertEquals(checks.size(), 1, "Expected one check response");

        JsonObject checkJson = checks.getJsonObject(0);
        Assert.assertEquals(SuccessReadiness.class.getName(), checkJson.getString("name"));
        verifySuccessStatus(checkJson);

        assertOverallSuccess(json);
    }

    @Test
    @OperateOnDeployment(ASYNC_DEPLOYMENT)
    public void testAdditionalPropertiesAsync() {
        Response response = getUrlHealthContents();

        Assert.assertEquals(response.getStatus(), 200);

        JsonObject json = readJson(response);

        // response size, SuccessLivenessAsync is in the deployment but it should not be included in the response
        JsonArray checks = json.getJsonArray("checks");
        Assert.assertEquals(checks.size(), 1, "Expected one check response");

        JsonObject checkJson = checks.getJsonObject(0);
        Assert.assertEquals(SuccessReadinessAsync.class.getName(), checkJson.getString("name"));
        verifySuccessStatus(checkJson);

        assertOverallSuccess(json);
    }
}
