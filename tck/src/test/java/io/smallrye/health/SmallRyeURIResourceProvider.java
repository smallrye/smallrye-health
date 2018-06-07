package io.smallrye.health;

import java.lang.annotation.Annotation;
import java.net.Inet6Address;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;

import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.arquillian.test.spi.enricher.resource.ResourceProvider;


public class SmallRyeURIResourceProvider implements ResourceProvider {
    @Override
    public boolean canProvide(Class<?> type) {
        return type.isAssignableFrom(URI.class);
    }

    @Override
    public Object lookup(ArquillianResource resource, Annotation... qualifiers) {
        return null;
    }

    URI toURI(InetSocketAddress addr) throws URISyntaxException {
        if (addr.getAddress() instanceof Inet6Address) {
            return new URI("http://[" + addr.getHostName() + "]:" + addr.getPort() + "/");
        }
        return new URI("http://" + addr.getHostName() + ":" + addr.getPort() + "/");
    }
}
