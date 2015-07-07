package com.brajagopal.rmend.be.service.resources;

import com.brajagopal.rmend.data.ResultsType;
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
@Path("v1/document")
public class DocumentResource extends BaseResource {

    static Logger logger = Logger.getLogger(DocumentResource.class);

    @Context
    Request request;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/topic/{topic}")
    public Response getDocumentByTopic(@PathParam("topic") String topic) {

        Response.Status responseStatus = Response.Status.INTERNAL_SERVER_ERROR;

        String errorMsg = "NA";
        Collection<DocumentBean> retVal = new ArrayList<>();
        try {
            retVal = getRecommender().getContentByTopic(getRecommender().makeTopicBean(topic), ResultsType.DEFAULT_RESULT_TYPE);
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

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{docNumber}")
    public Response getDocumentByNumber(@PathParam("docNumber") long docNumber) {

        Response.Status responseStatus = Response.Status.INTERNAL_SERVER_ERROR;

        String errorMsg = "NA";
        try {
            DocumentBean retVal = getDao().getDocument(docNumber);
            return Response.ok().entity(retVal).build();
        } catch (DatastoreException e) {
            logger.warn(e);
        } catch (DocumentNotFoundException e) {
            responseStatus = Response.Status.NOT_FOUND;
            errorMsg = e.getMessage();
            logger.warn(e);
        }
        return Response.status(responseStatus).header("X-Error-Msg", errorMsg).build();
    }
}
