package io.smallrye.health.deployment;

public class DependentCallRecorder {

    public static volatile boolean healthCheckPreDestroyCalled = false;
    public static volatile boolean asyncHealthCheckPreDestroyCalled = false;

}
