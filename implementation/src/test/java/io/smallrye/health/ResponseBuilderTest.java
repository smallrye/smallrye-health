package io.smallrye.health;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ResponseBuilderTest {

    @Test
    public void testWhenNameIsNullThrowsIllegalArgumentException() {
        final ResponseBuilder responseBuilder = new ResponseBuilder();
        Assertions.assertThrows(IllegalArgumentException.class, responseBuilder::build);
    }

    @Test
    public void testWhenNameIsEmptyStringThrowsIllegalArgumentException() {
        final ResponseBuilder responseBuilder = new ResponseBuilder();
        responseBuilder.name("");
        Assertions.assertThrows(IllegalArgumentException.class, responseBuilder::build);
    }
}