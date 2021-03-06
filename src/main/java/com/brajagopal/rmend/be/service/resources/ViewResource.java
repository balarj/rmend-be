package com.brajagopal.rmend.be.service.resources;

import com.brajagopal.rmend.app.beans.UserBean;
import com.brajagopal.rmend.app.beans.UserViewBean;
import com.brajagopal.rmend.be.entities.ImpressionItemsEntity;
import com.brajagopal.rmend.be.entities.ImpressionUsersEntity;
import com.brajagopal.rmend.be.entities.ViewEntity;
import com.brajagopal.rmend.exception.UserNotFoundException;
import com.google.common.base.Strings;
import org.apache.log4j.Logger;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * @author <bxr4261>
 */
@Path("/v1/view")
public class ViewResource extends BaseResource {

    static Logger logger = Logger.getLogger(UserResource.class);

    @Context
    HttpServletRequest request;

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/impression")
    public Response putUserView(UserViewBean userViewBean, @QueryParam("referrer") @DefaultValue("default") String referrer) {
        Response.ResponseBuilder response = Response.status(Response.Status.NOT_IMPLEMENTED);

        userViewBean.setReferrer(referrer);
        //TODO: Validate if the uid and docNum are valid
        EntityManager manager = getEntityManager("users-entity", logger);

        if (userViewBean.getIsInvalid()) {
            return response.status(Response.Status.BAD_REQUEST).build();
        }

        boolean isValid = false;
        if (!Strings.isNullOrEmpty(userViewBean.getUuid())) {
            try {
                UserBean bean = UserBaseResource.getUserByUUID(userViewBean.getUuid());
                userViewBean.setUid(bean.getUid());
                response.header("X-UUID", userViewBean.getUuid());
                isValid = true;
            } catch (UserNotFoundException e) {
                if (isValidationStrict()) {
                    return response
                            .header("X-Error-Msg", e.getMessage())
                            .header("X-UUID", userViewBean.getUuid())
                            .status(Response.Status.NOT_FOUND)
                            .build();
                }
            }
        }
        if (!isValid) {
            try {
                UserBean bean = UserBaseResource.getUserByUID(userViewBean.getUid());
                userViewBean.setUid(bean.getUid());
                response.header("X-UID", userViewBean.getUid());
            } catch (UserNotFoundException e) {
                if (isValidationStrict()) {
                    return response
                            .header("X-Error-Msg", e.getMessage())
                            .header("X-UID", userViewBean.getUid())
                            .status(Response.Status.NOT_FOUND)
                            .build();
                }
            }
        }


        try {
            manager.getTransaction().begin();

            // It is OK to view the same asset more than once!
            /*ViewEntity entity = manager.find(ViewEntity.class, userViewBean.getCompositeKey());
            if (entity != null) {
                throw new DuplicateEntryException(userViewBean.getCompositeKey());
            }*/

            ViewEntity viewEntity = ViewEntity.createInstance(userViewBean);

            ImpressionItemsEntity impressionItemsEntity =
                    ImpressionItemsEntity.createInstance(userViewBean.getDocNum());

            ImpressionUsersEntity impressionUsersEntity =
                    ImpressionUsersEntity.createInstance(userViewBean.getUid());

            manager.persist(viewEntity);
            manager.persist(impressionItemsEntity);
            manager.persist(impressionUsersEntity);
            manager.getTransaction().commit();
            response.header("X-Document-Number", viewEntity.getDocNum());
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
    public Response deleteUserViewsByUUID(@PathParam("uuid") String uuId) {
        Response.ResponseBuilder response = Response.serverError();

        UserBean userDetails = null;
        try {
            userDetails = UserBaseResource.getUserByUUID(uuId);
        } catch (UserNotFoundException e) {
            return response
                    .header("X-UUID", uuId)
                    .header("X-Error-Msg", e.getMessage())
                    .status(Response.Status.NOT_FOUND)
                    .build();
        }

        int deletedCount;
        try {
            deletedCount = doDelete(userDetails);
        }
        catch (Exception e) {
            logger.info(userDetails);
            logger.error(e);
            return response
                    .header("X-UUID", uuId)
                    .header("X-Error-Msg", e.getMessage())
                    .status(Response.Status.INTERNAL_SERVER_ERROR)
                    .build();
        }

        if (deletedCount == 0) {
            return response
                    .header("X-UUID", uuId)
                    .status(Response.Status.NOT_FOUND)
                    .build();
        }

        return response.status(Response.Status.OK)
                .header("X-UUID", uuId)
                .header("X-Deleted-Count", deletedCount)
                .build();
    }

    @DELETE
    @Path("/impression/uid/{uid}")
    public Response deleteUserViewsByUID(@PathParam("uid") Long uid) {
        Response.ResponseBuilder response = Response.serverError();

        UserBean userDetails = null;
        try {
            userDetails = UserBaseResource.getUserByUID(uid);
        } catch (UserNotFoundException e) {
            return response
                    .header("X-UID", uid)
                    .header("X-Error-Msg", e.getMessage())
                    .status(Response.Status.NOT_FOUND)
                    .build();
        }

        int deletedCount;
        try {
            deletedCount = doDelete(userDetails);
        }
        catch (Exception e) {
            logger.info(userDetails);
            logger.error(e);
            return response
                    .header("X-UUID", uid)
                    .header("X-Error-Msg", e.getMessage())
                    .status(Response.Status.INTERNAL_SERVER_ERROR)
                    .build();
        }

        if (deletedCount == 0) {
            return response
                    .header("X-UID", uid)
                    .status(Response.Status.NOT_FOUND)
                    .build();
        }

        return response.status(Response.Status.OK)
                .header("X-UID", uid)
                .header("X-Deleted-Count", deletedCount)
                .build();
    }

    @DELETE
    @Path("/impression/username/{username}")
    public Response deleteUserViewsByUsername(@PathParam("username") String username) {
        Response.ResponseBuilder response = Response.serverError();

        UserBean userDetails = null;
        try {
            userDetails = UserBaseResource.getUserByUsername(username);
        } catch (UserNotFoundException e) {
            return response
                    .header("X-Username", username)
                    .header("X-Error-Msg", e.getMessage())
                    .status(Response.Status.NOT_FOUND)
                    .build();
        }

        int deletedCount;
        try {
            deletedCount = doDelete(userDetails);
        }
        catch (Exception e) {
            logger.info(userDetails);
            logger.error(e);
            return response
                    .header("X-Username", username)
                    .header("X-Error-Msg", e.getMessage())
                    .status(Response.Status.INTERNAL_SERVER_ERROR)
                    .build();
        }

        if (deletedCount == 0) {
            return response
                    .header("X-Username", username)
                    .status(Response.Status.NOT_FOUND)
                    .build();
        }

        return response.status(Response.Status.OK)
                .header("X-Username", username)
                .header("X-Deleted-Count", deletedCount)
                .build();
    }

    private static int doDelete(UserBean _userBean) {
        //TODO: Validate if the uid and docNum are valid
        EntityManager manager = getEntityManager("users-entity", logger);

        int deletedCount;
        try {
            manager.getTransaction().begin();
            Query q = manager.createQuery("DELETE FROM " + ViewEntity.class.getName()
                    + " views WHERE views.uid = :u");
            deletedCount = q.setParameter("u", _userBean.getUid()).executeUpdate();
            manager.getTransaction().commit();
        }
        finally {
            if (manager.getTransaction().isActive()) {
                manager.getTransaction().rollback();
            }
            manager.close();
        }
        return deletedCount;
    }

    private boolean isValidationStrict() {
        String validationMode = request.getHeader("X-Validation-Mode");
        return (validationMode != null && validationMode.equalsIgnoreCase("STRICT"));
    }
}
