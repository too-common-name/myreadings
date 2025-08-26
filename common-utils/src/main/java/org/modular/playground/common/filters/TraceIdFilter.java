package org.modular.playground.common.filters;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;
import org.jboss.logging.MDC;
import java.io.IOException;
import java.util.UUID;

@Provider
public class TraceIdFilter implements ContainerRequestFilter, ContainerResponseFilter {

    public static final String TRACE_ID_KEY = "traceId";

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        String traceId = UUID.randomUUID().toString();
        MDC.put(TRACE_ID_KEY, traceId);
    }

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
        MDC.remove(TRACE_ID_KEY);
    }
}