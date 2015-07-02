package com.brajagopal.rmend.be.service.resources;

import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

/**
 * @author <bxr4261>
 */
@Path("v1/document")
public class DocumentResource {

    @Produces("application/json")
    @Path("/{topic}")
    public Response getDocumentByTopic(@PathParam("topic") String topic) {

        return Response.status(Response.Status.NOT_IMPLEMENTED).build();
    }
}
