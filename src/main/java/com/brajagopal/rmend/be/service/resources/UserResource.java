package com.brajagopal.rmend.be.service.resources;

import com.brajagopal.rmend.app.beans.UserBean;
import com.brajagopal.rmend.app.beans.UserViewBean;
import com.brajagopal.rmend.be.entities.UserEntity;
import com.brajagopal.rmend.be.entities.ViewEntity;
import com.brajagopal.rmend.exception.DuplicateEntryException;
import org.apache.log4j.Logger;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

/**
 * @author <bxr4261>
 */
@Path("/v1/user")
public class UserResource extends BaseResource {

    static Logger logger = Logger.getLogger(UserResource.class);

    @Context
    HttpServletRequest request;

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/uname/{userName}")
    public Response createUser(@PathParam("userName") String userName) {

        Response.ResponseBuilder response = Response.serverError();

        UserBean userBean = UserBean.create(userName);
        EntityManager manager = getEntityManager("users-entity");
        UserEntity user;
        try {
            manager.getTransaction().begin();
            UserEntity entity = manager.find(UserEntity.class, userName);
            if (entity != null) {
                throw new DuplicateEntryException(userName);
            }
            user = UserEntity.createInstance(userBean);
            manager.persist(user);
            manager.getTransaction().commit();
        }
        catch (DuplicateEntryException e) {
            logger.error(e);
            return response.header("X-Error-Msg", e.getMessage()).status(Response.Status.BAD_REQUEST).build();
        }
        catch (Exception e) {
            logger.error(e);
            return response.header("X-Error-Msg", e.getMessage()).status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
        finally {
            if (manager.getTransaction().isActive()) {
                manager.getTransaction().rollback();
            }
            manager.close();
        }

        return response.status(Response.Status.NO_CONTENT).header("X-UUID-Created", userBean.getUuid()).build();
    }


    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/uname/{userName}")
    public Response getUser(@PathParam("userName") String userName) {
        Response.ResponseBuilder response = Response.serverError();

        EntityManager manager = getEntityManager("users-entity");
        UserBean retVal;
        try {
            UserEntity userEntity = manager.find(UserEntity.class, userName);
            if (userEntity == null) {
                return response.status(Response.Status.NOT_FOUND).build();
            }
            retVal = userEntity.getUserBean();
        }
        catch (Exception e) {
            logger.error(e);
            return response.header("X-Error-Msg", e.getMessage()).status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
        finally {
            manager.close();
        }

        return response.status(Response.Status.OK).entity(retVal).build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/uuid/{uuid}")
    public Response getUserByUUID(@PathParam("uuid") String uuId) {
        Response.ResponseBuilder response = Response.serverError();

        //TODO: Validate UUID
        EntityManager manager = getEntityManager("users-entity");
        UserBean retVal;
        try {
            Query query = manager.createQuery("SELECT u FROM "
                    + UserEntity.class.getName()
                    + " u WHERE u.uuid = '" + uuId + "'");
            List<UserEntity> results = query.getResultList();
            if (results.isEmpty()) {
                return response.status(Response.Status.NOT_FOUND).header("X-UUID", uuId).build();
            }
            else if (results.size() > 1) {
                logger.error("Too many ("+results.size()+") entities with the same UUID ("+uuId+")");
                return response.status(Response.Status.INTERNAL_SERVER_ERROR).header("X-UUID", uuId).build();
            }
            retVal = results.get(0).getUserBean();
        }
        catch (Exception e) {
            logger.error(e);
            return response.header("X-Error-Msg", e.getMessage()).status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
        finally {
            manager.close();
        }

        return response.status(Response.Status.OK).entity(retVal).build();
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/view")
    public Response putUserView(UserViewBean userViewBean) {
        Response.ResponseBuilder response = Response.serverError();

        //TODO: Validate if the uid and docNum are valid
        EntityManager manager = getEntityManager("users-entity");

        try {
            manager.getTransaction().begin();
            ViewEntity entity = manager.find(ViewEntity.class, userViewBean.getCompositeKey());
            if (entity != null) {
                throw new DuplicateEntryException(userViewBean.getCompositeKey());
            }
            ViewEntity viewEntity = ViewEntity.createInstance(userViewBean);
            manager.persist(viewEntity);
            manager.getTransaction().commit();
        }
        catch (DuplicateEntryException e) {
            logger.error(e);
            return response.header("X-Error-Msg", e.getMessage()).status(Response.Status.BAD_REQUEST).build();
        }
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
}
