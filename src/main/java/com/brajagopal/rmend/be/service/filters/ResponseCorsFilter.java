package com.brajagopal.rmend.be.service.filters;

import com.google.api.client.repackaged.com.google.common.base.Strings;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import java.io.IOException;

/**
 * @author <bxr4261>
 */

@Priority(Priorities.HEADER_DECORATOR)
public class ResponseCorsFilter implements ContainerResponseFilter {

    @Override
    public void filter(ContainerRequestContext containerRequestContext, ContainerResponseContext containerResponseContext) throws IOException {
        // For CORS
        containerResponseContext
                .getHeaders().add("Access-Control-Allow-Origin", "*");

        containerResponseContext
                .getHeaders().add("Access-Control-Allow-Methods", "GET, PUT, POST, OPTIONS");

        String requestHeader = containerRequestContext.getHeaders().getFirst("Access-Control-Request-Headers");
        if (!Strings.isNullOrEmpty(requestHeader)) {
            containerResponseContext
                    .getHeaders().add("Access-Control-Allow-Headers", requestHeader);
        }

        containerResponseContext.getHeaders().add(
                "Access-Control-Expose-Headers",
                "X-Recommendation-Type, X-Recommendation-Method, X-Response-Count, X-Result-Type, X-Error-Msg"
        );
    }
}
