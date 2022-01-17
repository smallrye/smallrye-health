package io.smallrye.health;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class AsyncHealthCheckFactoryTest {

    private AsyncHealthCheckFactory asyncHealthCheckFactory;

    @BeforeEach
    public void beforeEach() {
        asyncHealthCheckFactory = new AsyncHealthCheckFactory();
    }

    @Test
    public void nulluncheckedExceptionDataStyleTest() {
        Assertions.assertThrows(NullPointerException.class, () -> asyncHealthCheckFactory.setUncheckedExceptionDataStyle(null));
    }
}
