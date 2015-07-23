package com.brajagopal.rmend.be.service.resources;

import com.brajagopal.rmend.be.beans.RecResponseBean;
import com.brajagopal.rmend.data.ResultsType;
import com.brajagopal.rmend.exception.DocumentNotFoundException;
import com.google.api.services.datastore.client.DatastoreException;
import org.apache.log4j.Logger;
import org.apache.mahout.cf.taste.common.NoSuchItemException;
import org.apache.mahout.cf.taste.common.TasteException;

import javax.ws.rs.*;
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
    @Path("/user/{userId}")
    public Response getUserRecommendation(
            @PathParam("userId") long userId,
            @DefaultValue("RANDOM_10") @QueryParam("resultType") String resultsTypeAsString) {

        Response.Status responseStatus = Response.Status.INTERNAL_SERVER_ERROR;
        ResultsType resultsType = ResultsType.DEFAULT_RESULT_TYPE;
        try {
            resultsType = ResultsType.valueOf(resultsTypeAsString);
        } catch (IllegalArgumentException e) {}

        String errorMsg = "NA";

        try {
            RecResponseBean responseBean =
                    getRecommender(RecommenderTypeEnum.COLLABORATIVE_FILTERING)
                            .getRecommendation(userId, ResultsType.RANDOM_10);

            if (responseBean.isEmpty()) {
                return Response.status(
                        Response.Status.NOT_FOUND)
                        .header("X-Result-Type", resultsType)
                        .build();
            }
            return Response.ok()
                    .entity(responseBean.getResults())
                    .header("X-Result-Type", resultsType)
                    .header("X-Recommendation-Type", responseBean.getSimilarityType())
                    .header("X-Response-Count", responseBean.size())
                    .build();
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


        return Response.status(responseStatus)
                .header("X-Error-Msg", errorMsg)
                .header("X-Result-Type", resultsType)
                .build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/document/{docId}")
    public Response getItemRecommendation(
            @PathParam("docId") long docId,
            @DefaultValue("RANDOM_10") @QueryParam("resultType") String resultsTypeAsString) {

        Response.Status responseStatus = Response.Status.INTERNAL_SERVER_ERROR;
        ResultsType resultsType = ResultsType.DEFAULT_RESULT_TYPE;
        try {
            resultsType = ResultsType.valueOf(resultsTypeAsString);
        } catch (IllegalArgumentException e) {}

        String errorMsg = "NA";

        try {
            RecResponseBean responseBean =
                    getRecommender(RecommenderTypeEnum.COLLABORATIVE_FILTERING)
                            .getItemSimilarity(docId, ResultsType.RANDOM_10);

            if (responseBean.isEmpty()) {
                return Response.status(
                        Response.Status.NOT_FOUND)
                        .header("X-Result-Type", resultsType)
                        .build();
            }
            return Response.ok()
                    .entity(responseBean.getResults())
                    .header("X-Result-Type", resultsType)
                    .header("X-Recommendation-Type", responseBean.getSimilarityType())
                    .header("X-Response-Count", responseBean.size())
                    .build();
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
        } catch (NoSuchItemException e) {
            errorMsg = e.getMessage() + " not found.";
            logger.warn(e);
        } catch (TasteException e) {
            errorMsg = e.getMessage();
            e.printStackTrace();
        }


        return Response.status(responseStatus)
                .header("X-Error-Msg", errorMsg)
                .header("X-Result-Type", resultsType)
                .build();
    }
}
