package io.smallrye.health;

import java.lang.annotation.Annotation;
import java.net.Inet6Address;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Set;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.CDI;

import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.arquillian.test.spi.enricher.resource.ResourceProvider;


public class SmallRyeURLResourceProvider implements ResourceProvider {
    @Override
    public boolean canProvide(Class<?> type) {
        return type.isAssignableFrom(URL.class);
    }

    @Override
    public Object lookup(ArquillianResource resource, Annotation... qualifiers) {
        return null;
    }

}
