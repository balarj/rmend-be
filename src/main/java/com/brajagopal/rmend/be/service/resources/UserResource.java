package com.brajagopal.rmend.be.service.resources;

import com.brajagopal.rmend.app.beans.UserBean;
import com.brajagopal.rmend.be.entities.UserEntity;
import com.brajagopal.rmend.exception.DuplicateEntryException;
import org.apache.log4j.Logger;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

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
    @Path("/create/{userName}")
    public Response createUser(@PathParam("userName") String userName) {

        Response.ResponseBuilder response = Response.serverError();

        UserBean userBean = UserBean.create(userName);
        EntityManagerFactory factory = Persistence.createEntityManagerFactory("users-entity");
        EntityManager manager = factory.createEntityManager();
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
            manager.getTransaction().rollback();
            return response.header("X-Error-Msg", e.getMessage()).status(Response.Status.BAD_REQUEST).build();
        }
        catch (Exception e) {
            logger.error(e);
            manager.getTransaction().rollback();
            return response.header("X-Error-Msg", e.getMessage()).status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
        finally {
            manager.close();
        }

        return response.status(Response.Status.NO_CONTENT).header("X-UUID-Created", userBean.getUuid()).build();
    }


}
