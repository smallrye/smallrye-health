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
        this.state = HealthCheckResponse.State.UP;
        return this;
    }

    @Override
    public HealthCheckResponseBuilder down() {
        this.state = HealthCheckResponse.State.DOWN;
        return this;
    }

    @Override
    public HealthCheckResponseBuilder state(boolean up) {
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

        return new Response(this.name, this.state, this.data.isEmpty() ? null : this.data);
    }

    private String name;

    private HealthCheckResponse.State state = HealthCheckResponse.State.DOWN;

    private Map<String, Object> data = new HashMap<>();
}
