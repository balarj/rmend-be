package com.brajagopal.rmend.be.service.resources;

import com.brajagopal.rmend.be.recommender.ContentRecommender;
import com.brajagopal.rmend.data.beans.DocumentBean;
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
import java.io.InvalidClassException;
import java.util.ArrayList;
import java.util.Collection;

/**
 * @author <bxr4261>
 */
@Path("v1/document")
public class DocumentResource extends BaseResource {

    static Logger logger = Logger.getLogger(DocumentResource.class);

    @Context
    Request request;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{topic}")
    public Response getDocumentByTopic(@PathParam("topic") String topic) {

        Response.ResponseBuilder builder = Response.serverError();
        String errorMsg = "NA";
        Collection<DocumentBean> retVal = new ArrayList<>();
        try {
            retVal = getRecommender().getContentByTopic(getRecommender().makeTopicBean(topic), ContentRecommender.ResultsType.TOP_5);
            if (retVal.isEmpty()) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
            return Response.ok().entity(retVal).build();
        } catch (DatastoreException e) {
            logger.warn(e);
        } catch (IllegalAccessException e) {
            errorMsg = e.getMessage();
            logger.warn(e);
        } catch (InvalidClassException e) {
            errorMsg = e.getMessage();
            logger.warn(e);
        } catch (InstantiationException e) {
            errorMsg = e.getMessage();
            logger.warn(e);
        }
        return Response.serverError().header("X-Error-Msg", errorMsg).build();

    }
}
