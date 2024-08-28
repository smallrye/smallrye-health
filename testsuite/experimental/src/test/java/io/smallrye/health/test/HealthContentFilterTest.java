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

import jakarta.json.JsonArray;
import jakarta.json.JsonObject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.shrinkwrap.api.Archive;
import org.testng.Assert;
import org.testng.annotations.Test;

import io.smallrye.health.deployment.SuccessLiveness;
import io.smallrye.health.deployment.TestHealthContentFilter;
import io.smallrye.health.deployment.TestHealthContentFilter2;

@RunAsClient
public class HealthContentFilterTest extends TCKBase {

    private static final String DEPLOYMENT_1 = "deployment1";
    private static final String DEPLOYMENT_2 = "deployment2";

    @Deployment(name = DEPLOYMENT_1)
    public static Archive getDeployment() {
        return DeploymentUtils.createWarFileWithClasses(HealthContentFilterTest.class.getSimpleName(),
                TestHealthContentFilter.class, SuccessLiveness.class, TCKBase.class);
    }

    @Deployment(name = DEPLOYMENT_2)
    public static Archive getDeployment2() {
        return DeploymentUtils.createWarFileWithClasses(HealthContentFilterTest.class.getSimpleName() + "2",
                TestHealthContentFilter.class, TestHealthContentFilter2.class, SuccessLiveness.class, TCKBase.class);
    }

    /**
     * Verifies that the filter implementations process the payload before its returned to the caller.
     */
    @Test
    @OperateOnDeployment(DEPLOYMENT_1)
    public void testHealthContentFilterFiltersReturnedJson() {
        Response response = getUrlHealthContents();

        // status code
        Assert.assertEquals(response.getStatus(), 200);

        JsonObject json = readJson(response);

        // response size
        JsonArray checks = json.getJsonArray("checks");
        Assert.assertEquals(checks.size(), 1, "Expected one check response");

        JsonObject checkJson = checks.getJsonObject(0);
        Assert.assertEquals(SuccessLiveness.class.getName(), checkJson.getString("name"));
        verifySuccessStatus(checkJson);

        assertOverallSuccess(json);

        System.out.println(json);
        Assert.assertEquals("bar", json.getString("foo"));
    }

    /**
     * Verifies that the filter implementations process the payload before its returned to the caller.
     */
    @Test
    @OperateOnDeployment(DEPLOYMENT_2)
    public void testMultipleHealthContentFiltersFilterReturnedJson() {
        Response response = getUrlHealthContents();

        // status code
        Assert.assertEquals(response.getStatus(), 200);

        JsonObject json = readJson(response);

        // response size
        JsonArray checks = json.getJsonArray("checks");
        Assert.assertEquals(checks.size(), 1, "Expected one check response");

        JsonObject checkJson = checks.getJsonObject(0);
        Assert.assertEquals(SuccessLiveness.class.getName(), checkJson.getString("name"));
        verifySuccessStatus(checkJson);

        assertOverallSuccess(json);

        System.out.println(json);
        Assert.assertEquals("bar", json.getString("foo"));
        Assert.assertEquals("bar2", json.getString("foo2"));
    }

}
