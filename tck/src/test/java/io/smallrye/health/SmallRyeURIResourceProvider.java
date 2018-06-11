package io.smallrye.health;

import java.lang.annotation.Annotation;
import java.net.URI;

import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.arquillian.test.spi.enricher.resource.ResourceProvider;

public class SmallRyeURIResourceProvider implements ResourceProvider {
    @Override
    public boolean canProvide(Class<?> type) {
        return type.isAssignableFrom(URI.class);
    }

    @Override
    public Object lookup(ArquillianResource resource, Annotation... qualifiers) {
        return URI.create("http://localhost:8080/");
    }
}
