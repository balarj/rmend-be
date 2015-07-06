package com.brajagopal.rmend.be.service.resources;

import com.brajagopal.rmend.be.recommender.ContentRecommender;
import com.brajagopal.rmend.data.beans.DocumentBean;
import com.brajagopal.rmend.exception.DocumentNotFoundException;
import com.google.api.services.datastore.client.DatastoreException;
import org.apache.log4j.Logger;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InvalidClassException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collection;

/**
 * @author <bxr4261>
 */
@Path("v1/recommend/content")
public class ContentRecommenderResource extends BaseResource {

    static Logger logger = Logger.getLogger(ContentRecommenderResource.class);

    @Context
    Request request;

    @Produces("application/json")
    @Path("/hello")
    public String sayHello() {
        return "Hello";
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{docNumber}")
    public Response getRecommendation(@PathParam("docNumber") long docNumber) {
        Response.Status responseStatus = Response.Status.INTERNAL_SERVER_ERROR;

        String errorMsg = "NA";
        Collection<DocumentBean> retVal = new ArrayList<>();
        try {
            retVal = getRecommender().getSimilarContent(docNumber, ContentRecommender.ResultsType.TOP_5);
            if (retVal.isEmpty()) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
            return Response.ok().entity(retVal).build();
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

        return Response.status(responseStatus).header("X-Error-Msg", errorMsg).build();
    }
}
