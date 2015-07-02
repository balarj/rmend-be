package com.brajagopal.rmend.be.service.resources;

import javax.ws.rs.Path;
import javax.ws.rs.Produces;

/**
 * @author <bxr4261>
 */
@Path("v1/content")
public class ContentRecommenderResource {

    @Produces("application/json")
    @Path("/hello")
    public String sayHello() {
        return "Hello";
    }
}
