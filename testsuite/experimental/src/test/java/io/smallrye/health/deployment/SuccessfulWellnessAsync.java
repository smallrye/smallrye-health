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
package io.smallrye.health.deployment;

import java.time.Duration;

import javax.enterprise.context.ApplicationScoped;

import org.eclipse.microprofile.health.HealthCheckResponse;

import io.smallrye.health.api.AsyncHealthCheck;
import io.smallrye.health.api.Wellness;
import io.smallrye.mutiny.Uni;

@Wellness
@ApplicationScoped
public class SuccessfulWellnessAsync implements AsyncHealthCheck {
    @Override
    public Uni<HealthCheckResponse> call() {
        return Uni.createFrom().item(HealthCheckResponse.up("successful-check"))
                .onItem().delayIt().by(Duration.ofMillis(10));
    }
}
