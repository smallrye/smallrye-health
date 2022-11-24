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

import io.smallrye.health.deployment.DefaultGroupExplicit;
import io.smallrye.health.deployment.HealthGroup1;
import io.smallrye.health.deployment.HealthGroup12;
import io.smallrye.health.deployment.SuccessLiveness;

@RunAsClient
public class HealthGroupsTest extends TCKBase {

    private static final String DEPLOYMENT_NO_DEFAULT = "deployment";
    private static final String DEPLOYMENT_DEFAULT_GROUP = "deploymentDefaultGroup";
    private static final String DEPLOYMENT_DEFAULT_GROUP_EXPLICIT = "deploymentDefaultGroupExplicit";

    @Deployment(name = DEPLOYMENT_NO_DEFAULT)
    public static Archive getDeploymentNoDefault() {
        return DeploymentUtils.createWarFileWithClasses(HealthGroupsTest.class.getSimpleName() + "-no-default",
                SuccessLiveness.class, HealthGroup1.class, HealthGroup12.class, TCKBase.class);
    }

    @Deployment(name = DEPLOYMENT_DEFAULT_GROUP)
    public static Archive getDeploymentDefaultGroup() {
        return DeploymentUtils.createWarFileWithClasses(HealthGroupsTest.class.getSimpleName() + "-default-group",
                SuccessLiveness.class, HealthGroup1.class, HealthGroup12.class,
                HealthGroup12.class, TCKBase.class)
                .addAsManifestResource(
                        new StringAsset("io.smallrye.health.defaultHealthGroup=default-group"),
                        "microprofile-config.properties");
    }

    @Deployment(name = DEPLOYMENT_DEFAULT_GROUP_EXPLICIT)
    public static Archive getDeploymentDefaultGroupExplicit() {
        return DeploymentUtils.createWarFileWithClasses(HealthGroupsTest.class.getSimpleName() + "-default-group-explicit",
                SuccessLiveness.class, HealthGroup1.class, HealthGroup12.class,
                HealthGroup12.class, DefaultGroupExplicit.class, TCKBase.class)
                .addAsManifestResource(
                        new StringAsset("io.smallrye.health.defaultHealthGroup=default-group"),
                        "microprofile-config.properties");
    }

    @Test
    @OperateOnDeployment(DEPLOYMENT_NO_DEFAULT)
    public void testHealthGroupsNoDefault() {
        verifyManuallySpecifiedHealthGroups();

        // verify that default group is not included if not specified in config
        Response response = getUrlCustomHealthContents("default-group");

        Assert.assertEquals(response.getStatus(), 200);

        JsonObject json = readJson(response);

        JsonArray checks = json.getJsonArray("checks");
        Assert.assertEquals(checks.size(), 0, "Expected no checks in default-group");

        assertOverallSuccess(json);
    }

    @Test
    @OperateOnDeployment(DEPLOYMENT_DEFAULT_GROUP)
    public void testHealthGroupsWithDefaultGroup() {
        verifyManuallySpecifiedHealthGroups();

        // verify that default group is included if specified in config
        Response response = getUrlCustomHealthContents("default-group");

        Assert.assertEquals(response.getStatus(), 200);

        JsonObject json = readJson(response);

        JsonArray checks = json.getJsonArray("checks");
        Assert.assertEquals(checks.size(), 1, "Expected checks without @HealthGroup to be included in default-group");

        JsonObject checkJson = checks.getJsonObject(0);
        Assert.assertEquals(SuccessLiveness.class.getName(), checkJson.getString("name"));
        verifySuccessStatus(checkJson);

        assertOverallSuccess(json);
    }

    @Test
    @OperateOnDeployment(DEPLOYMENT_DEFAULT_GROUP_EXPLICIT)
    public void testHealthGroupsWithDefaultGroupExplicit() {
        verifyManuallySpecifiedHealthGroups();

        // verify that default group and explicitly defined default group is included if specified in config
        Response response = getUrlCustomHealthContents("default-group");

        Assert.assertEquals(response.getStatus(), 200);

        JsonObject json = readJson(response);

        JsonArray checks = json.getJsonArray("checks");
        Assert.assertEquals(checks.size(), 2, "Expected two check responses");

        for (JsonObject check : checks.getValuesAs(JsonObject.class)) {
            String id = check.getString("name");

            if (id.equals(SuccessLiveness.class.getName()) || id.equals(DefaultGroupExplicit.class.getName())) {
                verifySuccessStatus(check);
            } else {
                Assert.fail("Unexpected response payload structure");
            }
        }

        assertOverallSuccess(json);
    }

    private void verifyManuallySpecifiedHealthGroups() {
        // verify individual manually specified groups
        Response response = getUrlCustomHealthContents("health-group-1");

        Assert.assertEquals(response.getStatus(), 200);

        JsonObject json = readJson(response);

        JsonArray checks = json.getJsonArray("checks");
        Assert.assertEquals(checks.size(), 2, "Expected two check responses");

        for (JsonObject check : checks.getValuesAs(JsonObject.class)) {
            String id = check.getString("name");
            switch (id) {
                case "health-group-1":
                case "health-group-12":
                    verifySuccessStatus(check);
                    break;
                default:
                    Assert.fail("Unexpected response payload structure");
            }
        }

        assertOverallSuccess(json);
    }
}
