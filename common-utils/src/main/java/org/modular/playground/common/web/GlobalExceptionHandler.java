package org.modular.playground.common.web;

import org.jboss.logging.Logger;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class GlobalExceptionHandler implements ExceptionMapper<Exception> {
    
    private static final Logger LOGGER = Logger.getLogger(GlobalExceptionHandler.class);

    @Override
    public Response toResponse(Exception exception) {
        if (exception instanceof WebApplicationException) {
            return ((WebApplicationException) exception).getResponse();
        }
        
        LOGGER.error("Unhandled exception caught by global handler", exception);

        String jsonError = "{\"error\":\"An internal server error occurred. Please reference the traceId in the logs.\"}";
        
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                       .entity(jsonError)
                       .type(MediaType.APPLICATION_JSON)
                       .build();
    }
}