package io.smallrye.health.deployment;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.json.Json;
import jakarta.json.JsonObject;

import io.smallrye.health.api.HealthContentFilter;

@ApplicationScoped
public class TestHealthContentFilter2 implements HealthContentFilter {

    @Override
    public JsonObject filter(JsonObject payload) {
        return Json.createObjectBuilder(payload)
                .add("foo2", "bar2")
                .build();
    }
}
