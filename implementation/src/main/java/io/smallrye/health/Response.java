package io.smallrye.health;

import java.util.Map;
import java.util.Optional;

import org.eclipse.microprofile.health.HealthCheckResponse;

class Response extends HealthCheckResponse {

    Response(String name, Status status, Map<String, Object> data) {
        this.name = name;
        this.status = status;
        this.data = data;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public Status getStatus() {
        return this.status;
    }

    @Override
    public Optional<Map<String, Object>> getData() {
        return Optional.ofNullable(this.data);
    }

    private final String name;

    private final Status status;

    private final Map<String, Object> data;
}
