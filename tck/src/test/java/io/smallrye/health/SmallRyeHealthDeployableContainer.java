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

import org.jboss.arquillian.container.spi.client.container.ContainerConfiguration;
import org.jboss.arquillian.container.spi.client.container.DeployableContainer;
import org.jboss.arquillian.container.spi.client.container.DeploymentException;
import org.jboss.arquillian.container.spi.client.container.LifecycleException;
import org.jboss.arquillian.container.spi.client.protocol.ProtocolDescription;
import org.jboss.arquillian.container.spi.client.protocol.metadata.ProtocolMetaData;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.descriptor.api.Descriptor;
import org.jboss.weld.environment.servlet.Container;
import org.jboss.weld.environment.servlet.Listener;
import org.jboss.weld.environment.undertow.UndertowContainer;

import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.PathHandler;
import io.undertow.server.handlers.resource.ClassPathResourceManager;
import io.undertow.servlet.Servlets;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.DeploymentManager;

@SuppressWarnings("rawtypes")
public class SmallRyeHealthDeployableContainer implements DeployableContainer {

    private Undertow server;
    
    @Override
    public ProtocolMetaData deploy(Archive arg0) throws DeploymentException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void deploy(Descriptor arg0) throws DeploymentException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public Class<?> getConfigurationClass() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ProtocolDescription getDefaultProtocol() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setup(ContainerConfiguration arg0) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void start() throws LifecycleException {
        
        try {
            DeploymentInfo servletBuilder = Servlets.deployment().setClassLoader(SmallRyeHealthDeployableContainer.class.getClassLoader())
                    .setResourceManager(new ClassPathResourceManager(SmallRyeHealthDeployableContainer.class.getClassLoader()))
                    .setContextPath("/health")
                    .setDeploymentName("test.war")
                    // Weld listener
                    .addListener(Servlets.listener(Listener.class))
                    // application components
                    .addServlet(Servlets.servlet(SmallRyeHealthServlet.class).addMapping("/*").setLoadOnStartup(1))
                    .addInitParameter(Container.CONTEXT_PARAM_CONTAINER_CLASS, UndertowContainer.class.getName());
            
            DeploymentManager manager = Servlets.defaultContainer().addDeployment(servletBuilder);
            manager.deploy();
            HttpHandler servletHandler = manager.start();
            PathHandler path = Handlers.path(Handlers.redirect("/")).addPrefixPath("/", servletHandler);
            server = Undertow.builder().addHttpListener(8080, "localhost").setHandler(path).build();
            server.start();
        } catch (Exception ex) {
            throw new LifecycleException("Failed to start Undertow", ex);
        }
    }

    @Override
    public void stop() throws LifecycleException {
        if (server != null) {
            server.stop();
        }
        
    }

    @Override
    public void undeploy(Archive arg0) throws DeploymentException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void undeploy(Descriptor arg0) throws DeploymentException {
        // TODO Auto-generated method stub
        
    }

}
