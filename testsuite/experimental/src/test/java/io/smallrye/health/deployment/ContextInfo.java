package io.smallrye.health.deployment;

import jakarta.enterprise.context.RequestScoped;

@RequestScoped
public class ContextInfo {

    private String value;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
