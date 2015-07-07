package com.brajagopal.rmend.be.service.resources;

import org.apache.log4j.Logger;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;

/**
 * @author <bxr4261>
 */
@Path("v1/recommend/cf")
public class CFRecommenderResource extends BaseResource {

    static Logger logger = Logger.getLogger(CFRecommenderResource.class);

    @Context
    Request request;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/document/{docNumber}")
    public Response getRecommendation(@PathParam("docNumber") long docNumber) {
        Response.Status responseStatus = Response.Status.NOT_IMPLEMENTED;
        String errorMsg = "NA";

        return Response.status(responseStatus).header("X-Error-Msg", errorMsg).build();
    }
}
