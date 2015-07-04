package com.brajagopal.rmend.be.service.resources;

import com.brajagopal.rmend.be.recommender.ContentRecommender;
import com.brajagopal.rmend.dao.GCloudDao;
import com.brajagopal.rmend.dao.IRMendDao;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.security.GeneralSecurityException;

/**
 * @author <bxr4261>
 */
public class BaseResource {

    private static final Logger logger = Logger.getLogger(BaseResource.class);
    private static IRMendDao dao;
    private static ContentRecommender contentRecommender;

    static {
        try {
            dao = new GCloudDao();
        } catch (GeneralSecurityException e) {
            logger.warn(e);
        } catch (IOException e) {
            logger.warn(e);
        }
    }

    public IRMendDao getDao() {
        return dao;
    }

    public ContentRecommender getRecommender() {
        if (contentRecommender == null) {
            contentRecommender = new ContentRecommender(dao);
        }
        return contentRecommender;
    }
}
