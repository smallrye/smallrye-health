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

import javax.json.JsonArray;
import javax.json.JsonObject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.testng.Assert;
import org.testng.annotations.Test;

import io.smallrye.health.deployment.SuccessLiveness;

@RunAsClient
public class AdditionalHealthPropertiesTest extends TCKBase {

    @Deployment
    public static Archive getDeployment() {
        return DeploymentUtils.createWarFileWithClasses(AdditionalHealthPropertiesTest.class.getSimpleName(),
                SuccessLiveness.class, TCKBase.class)
                .addAsManifestResource(new StringAsset("io.smallrye.health.additional.property.testProperty1=testValue1\n" +
                        "io.smallrye.health.additional.property.testProperty2=testValue2\n" +
                        "io.smallrye.health.additional.property.testProperty3=testValue3"),
                        "microprofile-config.properties");
    }

    @Test
    public void testAdditionalProperties() {
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

        Assert.assertEquals(json.getString("testProperty1", "no value provided"), "testValue1");
        Assert.assertEquals(json.getString("testProperty2", "no value provided"), "testValue2");
        Assert.assertEquals(json.getString("testProperty3", "no value provided"), "testValue3");
    }
}
