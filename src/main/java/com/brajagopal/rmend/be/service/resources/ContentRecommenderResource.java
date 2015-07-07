package com.brajagopal.rmend.be.service.resources;

import com.brajagopal.rmend.data.ResultsType;
import com.brajagopal.rmend.data.beans.DocumentBean;
import com.brajagopal.rmend.exception.DocumentNotFoundException;
import com.google.api.services.datastore.client.DatastoreException;
import org.apache.log4j.Logger;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InvalidClassException;
import java.security.GeneralSecurityException;
import java.util.Collection;

/**
 * @author <bxr4261>
 */
@Path("v1/recommend/content")
public class ContentRecommenderResource extends BaseResource {

    static Logger logger = Logger.getLogger(ContentRecommenderResource.class);

    @Context
    Request request;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/document/{docNumber}")
    public Response getRecommendation(@PathParam("docNumber") long docNumber, @DefaultValue("RANDOM_10") @QueryParam("resultType") String resultsTypeAsString) {
        Response.Status responseStatus = Response.Status.INTERNAL_SERVER_ERROR;

        ResultsType resultsType = ResultsType.DEFAULT_RESULT_TYPE;
        try {
            resultsType = ResultsType.valueOf(resultsTypeAsString);
        } catch (IllegalArgumentException e) {}

        String errorMsg = "NA";
        try {
            Collection<DocumentBean> retVal = getRecommender().getSimilarContent(docNumber, resultsType);
            if (retVal.isEmpty()) {
                return Response.status(
                        Response.Status.NOT_FOUND)
                        .header("X-Result-Type", resultsType)
                        .build();
            }
            return Response.ok()
                    .entity(retVal)
                    .header("X-Result-Type", resultsType)
                    .build();
        } catch (DatastoreException e) {
            logger.warn(e);
        } catch (InvalidClassException e) {
            errorMsg = e.getMessage();
            logger.warn(e);
        } catch (GeneralSecurityException e) {
            errorMsg = e.getMessage();
            logger.warn(e);
        } catch (IOException e) {
            errorMsg = e.getMessage();
            logger.warn(e);
        } catch (DocumentNotFoundException e) {
            responseStatus = Response.Status.NOT_FOUND;
            errorMsg = e.getMessage();
            logger.warn(e);
        }

        return Response.status(responseStatus)
                .header("X-Error-Msg", errorMsg)
                .header("X-Result-Type", resultsType)
                .build();
    }
}
