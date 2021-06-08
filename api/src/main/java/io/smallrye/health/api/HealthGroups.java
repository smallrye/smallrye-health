/*
 * Copyright (c) 2017-2020 Contributors to the Eclipse Foundation
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
package io.smallrye.health.api;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import io.smallrye.common.annotation.Experimental;

/**
 *
 * This annotation allow to repeat {@link HealthGroup} qualifier
 *
 * @author Antoine Sabot-Durand
 *
 */
@Target({ PARAMETER, FIELD, METHOD, TYPE })
@Documented
@Retention(RUNTIME)
@Experimental("Custom health group definitions exposed at /health/group/{group-name}. Not covered by the specification. " +
        "Subject to change.")
public @interface HealthGroups {
    /**
     * @return list of {@link HealthGroup}
     */
    HealthGroup[] value();
}
