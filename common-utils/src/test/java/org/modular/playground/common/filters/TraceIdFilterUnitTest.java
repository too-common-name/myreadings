package org.modular.playground.common.filters;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import org.jboss.logging.MDC;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

public class TraceIdFilterUnitTest {

    @AfterEach
    void tearDown() {
        MDC.clear();
    }

    @Test
    void shouldSetTraceIdOnRequest() throws IOException {
        TraceIdFilter filter = new TraceIdFilter();
        ContainerRequestContext requestContext = mock(ContainerRequestContext.class);

        filter.filter(requestContext);

        assertNotNull(MDC.get(TraceIdFilter.TRACE_ID_KEY));
    }

    @Test
    void shouldRemoveTraceIdOnResponse() throws IOException {
        TraceIdFilter filter = new TraceIdFilter();
        ContainerRequestContext requestContext = mock(ContainerRequestContext.class);
        ContainerResponseContext responseContext = mock(ContainerResponseContext.class);

        MDC.put(TraceIdFilter.TRACE_ID_KEY, "test-trace-id");
        filter.filter(requestContext, responseContext);
    }
}