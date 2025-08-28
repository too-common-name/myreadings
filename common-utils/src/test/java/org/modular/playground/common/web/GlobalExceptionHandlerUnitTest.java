package org.modular.playground.common.web;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

public class GlobalExceptionHandlerUnitTest {

    @Test
    void shouldReturn500WhenExceptionIsGeneric() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        Exception genericException = new RuntimeException("Internal error");
        String expectedJson = "{\"error\":\"An internal server error occurred. Please reference the traceId in the logs.\"}";

        Response response = handler.toResponse(genericException);

        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
        assertEquals(MediaType.APPLICATION_JSON, response.getMediaType().toString());
        assertEquals(expectedJson, response.getEntity());
    }

    @Test
    void shouldReturnOriginalResponseWhenExceptionIsWebApplicationException() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        Response originalResponse = Response.status(Response.Status.NOT_FOUND).entity("Not Found").build();
        WebApplicationException webAppException = new WebApplicationException(originalResponse);

        Response response = handler.toResponse(webAppException);

        assertSame(originalResponse, response);
    }
}