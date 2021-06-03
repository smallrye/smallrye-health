package io.smallrye.health.registry;

import javax.enterprise.context.ApplicationScoped;

import org.eclipse.microprofile.health.Startup;

import io.smallrye.health.api.HealthRegistry;

@Startup
@ApplicationScoped
public class StartupHealthRegistry extends AbstractHealthRegistry implements HealthRegistry {
}
