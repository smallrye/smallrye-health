package io.smallrye.health;

import org.junit.Test;

public class ResponseBuilderTest {

    @Test(expected = IllegalArgumentException.class)
    public void testWhenNameIsNullThrowsIllegalArgumentException() {
        final ResponseBuilder responseBuilder = new ResponseBuilder();
        responseBuilder.build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWhenNameIsEmptyStringThrowsIllegalArgumentException() {
        final ResponseBuilder responseBuilder = new ResponseBuilder();
        responseBuilder.name("");
        responseBuilder.build();
    }
}