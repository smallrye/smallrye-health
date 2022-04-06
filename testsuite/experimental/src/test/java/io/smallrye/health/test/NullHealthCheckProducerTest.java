/*
 * Copyright (c) 2021 Contributors to the Eclipse Foundation
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
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.shrinkwrap.api.Archive;
import org.testng.Assert;
import org.testng.annotations.Test;

import io.smallrye.health.deployment.NullHealthCheckProducer;

/**
 * @author Martin Stefanko
 */
public class NullHealthCheckProducerTest extends TCKBase {

    @Deployment
    public static Archive getDeployment() {
        return DeploymentUtils.createWarFileWithClasses(NullHealthCheckProducerTest.class.getSimpleName(),
                NullHealthCheckProducer.class, TCKBase.class);
    }

    /**
     * Verifies the CDI producer producin a null HealthCheck (with scope @Depedent) does not
     * throw NullPointerException
     */
    @Test
    @RunAsClient
    public void testSuccessResponsePayload() {
        Response response = getUrlLiveContents();

        // status code
        Assert.assertEquals(response.getStatus(), 200);

        JsonObject json = readJson(response);

        // response size
        JsonArray checks = json.getJsonArray("checks");
        Assert.assertEquals(checks.size(), 0, "Expected a null health check is ignored in the output");

        assertOverallSuccess(json);
    }
}
