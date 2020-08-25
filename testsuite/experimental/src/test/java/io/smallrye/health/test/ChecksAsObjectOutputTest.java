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

import javax.json.JsonObject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.testng.Assert;
import org.testng.annotations.Test;

import io.smallrye.health.deployment.SuccessLiveness;
import io.smallrye.health.deployment.SuccessReadiness;

public class ChecksAsObjectOutputTest extends TCKBase {

    @Deployment
    public static Archive getDeployment() {
        return DeploymentUtils.createWarFileWithClasses(ChecksAsObjectOutputTest.class.getSimpleName(),
                SuccessLiveness.class, SuccessReadiness.class)
                .addAsManifestResource(new StringAsset("io.smallrye.health.response.checks.object=true"),
                        "microprofile-config.properties");
    }

    /**
     * Verifies the custom output format set by io.smallrye.health.response.checks.object config property.
     */
    @Test
    @RunAsClient
    public void testPayload() {
        Response response = getUrlHealthContents();

        // status code
        Assert.assertEquals(response.getStatus(), 200);

        JsonObject json = readJson(response);

        // response
        JsonObject checks = json.getJsonObject("checks");
        Assert.assertNotNull(checks, "Checks object was not included in the response");

        JsonObject liveness = checks.getJsonObject(SuccessLiveness.class.getName());
        Assert.assertNotNull(liveness, "Expected liveness check not included in the response");
        verifySuccessStatus(liveness);

        JsonObject readiness = checks.getJsonObject(SuccessReadiness.class.getName());
        Assert.assertNotNull(readiness, "Expected readiness check not included in the response");

        assertOverallSuccess(json);
    }

}
