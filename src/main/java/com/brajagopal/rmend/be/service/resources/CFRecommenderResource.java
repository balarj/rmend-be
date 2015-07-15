package com.brajagopal.rmend.be.service.resources;

import com.brajagopal.rmend.data.ResultsType;
import com.brajagopal.rmend.exception.DocumentNotFoundException;
import com.google.api.services.datastore.client.DatastoreException;
import org.apache.log4j.Logger;
import org.apache.mahout.cf.taste.common.TasteException;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.security.GeneralSecurityException;

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

        try {
            getRecommender(RecommenderTypeEnum.COLLABORATIVE_FILTERING)
                    .getRecommendation(docNumber, ResultsType.RANDOM_10);
        } catch (DatastoreException e) {
            logger.warn(e);
        } catch (DocumentNotFoundException e) {
            logger.warn(e);
        } catch (GeneralSecurityException e) {
            errorMsg = e.getMessage();
            logger.warn(e);
        } catch (IOException e) {
            errorMsg = e.getMessage();
            logger.warn(e);
        } catch (TasteException e) {
            e.printStackTrace();
        }


        return Response.status(responseStatus).header("X-Error-Msg", errorMsg).build();
    }
}
