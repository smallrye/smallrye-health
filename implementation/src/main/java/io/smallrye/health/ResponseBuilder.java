package io.smallrye.health;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.HealthCheckResponseBuilder;

class ResponseBuilder extends HealthCheckResponseBuilder {

    @Override
    public HealthCheckResponseBuilder name(String name) {
        this.name = name;
        return this;
    }

    @Override
    public HealthCheckResponseBuilder withData(String key, String value) {
        this.data.put(key, value);
        return this;
    }

    @Override
    public HealthCheckResponseBuilder withData(String key, long value) {
        this.data.put(key, value);
        return this;
    }

    @Override
    public HealthCheckResponseBuilder withData(String key, boolean value) {
        this.data.put(key, value);
        return this;
    }

    @Override
    public HealthCheckResponseBuilder up() {
        this.status = HealthCheckResponse.Status.UP;
        return this;
    }

    @Override
    public HealthCheckResponseBuilder down() {
        this.status = HealthCheckResponse.Status.DOWN;
        return this;
    }

    @Override
    public HealthCheckResponseBuilder status(boolean up) {
        if (up) {
            return up();
        }

        return down();
    }

    @Override
    public HealthCheckResponse build() {
        if (null == this.name || this.name.trim().length() == 0) {
            throw HealthMessages.msg.invalidHealthCheckName();
        }

        return new Response(this.name, this.status, this.data.isEmpty() ? null : this.data);
    }

    private String name;

    private HealthCheckResponse.Status status = HealthCheckResponse.Status.DOWN;

    private Map<String, Object> data = new HashMap<>();
}
