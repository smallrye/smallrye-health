/*
 * Copyright 2017 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.smallrye.health;

import java.net.URL;

import org.jboss.arquillian.container.test.spi.client.deployment.ApplicationArchiveProcessor;
import org.jboss.arquillian.test.spi.TestClass;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.spec.WebArchive;

public class SmallRyeWarArchiveProcessor implements ApplicationArchiveProcessor {
    @Override
    public void process(Archive<?> appArchive, TestClass testClass) {
        if (!(appArchive instanceof WebArchive)) {
            return;
        }
        WebArchive war = WebArchive.class.cast(appArchive);
        URL webXml = SmallRyeWarArchiveProcessor.class.getResource("/WEB-INF/web.xml");
        if (webXml != null) {
            war.setWebXML(webXml);
        }

    }

}
