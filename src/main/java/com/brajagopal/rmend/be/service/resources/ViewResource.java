package com.brajagopal.rmend.be.service.resources;

import com.brajagopal.rmend.app.beans.UserBean;
import com.brajagopal.rmend.app.beans.UserViewBean;
import com.brajagopal.rmend.be.entities.ViewEntity;
import com.brajagopal.rmend.exception.UserNotFoundException;
import org.apache.log4j.Logger;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * @author <bxr4261>
 */
@Path("/v1/view")
public class ViewResource extends BaseResource {

    static Logger logger = Logger.getLogger(UserResource.class);

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/impression")
    public Response putUserView(UserViewBean userViewBean) {
        Response.ResponseBuilder response = Response.serverError();

        //TODO: Validate if the uid and docNum are valid
        EntityManager manager = getEntityManager("users-entity", logger);

        try {
            manager.getTransaction().begin();
            ViewEntity entity = manager.find(ViewEntity.class, userViewBean.getCompositeKey());
            /*if (entity != null) {
                throw new DuplicateEntryException(userViewBean.getCompositeKey());
            }*/
            ViewEntity viewEntity = ViewEntity.createInstance(userViewBean);
            manager.persist(viewEntity);
            manager.getTransaction().commit();
        }
        /*catch (DuplicateEntryException e) {
            logger.error(e);
            return response.header("X-Error-Msg", e.getMessage()).status(Response.Status.BAD_REQUEST).build();
        }*/
        catch (Exception e) {
            logger.error(e);
            return response.header("X-Error-Msg", e.getMessage()).status(Response.Status.INTERNAL_SERVER_ERROR).build();
        } finally {
            if (manager.getTransaction().isActive()) {
                manager.getTransaction().rollback();
            }
            manager.close();
        }
        return response.status(Response.Status.NO_CONTENT).build();
    }

    @DELETE
    @Path("/impression/uuid/{uuid}")
    public Response deleteUserViews(@PathParam("uuid") String uuId) {
        Response.ResponseBuilder response = Response.serverError();

        //TODO: Validate if the uid and docNum are valid
        EntityManager manager = getEntityManager("users-entity", logger);

        int deletedCount;
        try {
            UserBean userDetails = UserBaseResource.getUserByUUID(uuId);
            manager.getTransaction().begin();
            Query q = manager.createQuery("DELETE FROM " + ViewEntity.class.getName()
                    + " views WHERE views.uid = :u");
            deletedCount = q.setParameter("u", userDetails.getUid()).executeUpdate();
            if (deletedCount == 0) {
                return response
                        .header("X-Deleted-Count", deletedCount)
                        .header("X-UUID", uuId)
                        .status(Response.Status.NOT_FOUND)
                        .build();
            }

            manager.getTransaction().commit();
        }
        catch (UserNotFoundException e) {
            return response
                    .header("X-UUID", uuId)
                    .header("X-Error-Msg", e.getMessage())
                    .status(Response.Status.NOT_FOUND)
                    .build();
        }
        catch (Exception e) {
            logger.error(e);
            return response
                    .header("X-UUID", uuId)
                    .header("X-Error-Msg", e.getMessage())
                    .status(Response.Status.INTERNAL_SERVER_ERROR)
                    .build();
        }
        finally {
            if (manager.getTransaction().isActive()) {
                manager.getTransaction().rollback();
            }
            manager.close();
        }

        return response.status(Response.Status.OK)
                .header("X-UUID", uuId)
                .header("X-Deleted-Count", deletedCount)
                .build();
    }
}
