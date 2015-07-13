package com.brajagopal.rmend.be.service.resources;

import com.brajagopal.rmend.app.beans.UserBean;
import com.brajagopal.rmend.be.entities.UserEntity;
import com.brajagopal.rmend.exception.UserNotFoundException;
import org.apache.log4j.Logger;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.List;

/**
 * @author <bxr4261>
 */
public class UserBaseResource extends BaseResource {

    private static Logger logger = Logger.getLogger(UserResource.class);

    public static UserBean getUserByUUID(String uuId) throws UserNotFoundException {
        EntityManager manager = getEntityManager("users-entity", logger);
        UserBean retVal;
        try {
            Query query = manager.createQuery("SELECT u FROM "
                    + UserEntity.class.getName()
                    + " u WHERE u.uuid = '" + uuId + "'");
            List<UserEntity> results = query.getResultList();
            if (results.isEmpty()) {
                throw new UserNotFoundException(uuId);
            }
            else if (results.size() > 1) {
                throw new UnsupportedOperationException(
                        "Too many ("+results.size()+") entities with the same UUID ("+uuId+")");
            }
            return results.get(0).getUserBean();
        }
        finally {
            manager.close();
        }
    }

    public static UserBean getUserByUID(Long uid) throws UserNotFoundException {
        EntityManager manager = getEntityManager("users-entity", logger);
        UserBean retVal;
        try {
            Query query = manager.createQuery("SELECT u FROM "
                    + UserEntity.class.getName()
                    + " u WHERE u.uid = " + uid + "");
            List<UserEntity> results = query.getResultList();
            if (results.isEmpty()) {
                throw new UserNotFoundException(uid);
            }
            else if (results.size() > 1) {
                throw new UnsupportedOperationException(
                        "Too many ("+results.size()+") entities with the same UID ("+uid+")");
            }
            return results.get(0).getUserBean();
        }
        finally {
            manager.close();
        }
    }
}
