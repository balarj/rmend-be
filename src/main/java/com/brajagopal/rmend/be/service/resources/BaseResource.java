package com.brajagopal.rmend.be.service.resources;

import com.brajagopal.rmend.be.recommender.CFRecommender;
import com.brajagopal.rmend.be.recommender.ContentRecommender;
import com.brajagopal.rmend.be.recommender.IRecommender;
import com.brajagopal.rmend.dao.GCloudDao;
import com.brajagopal.rmend.dao.IRMendDao;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.jackson.JacksonFactory;
import com.google.api.services.datastore.client.DatastoreOptions;
import org.apache.log4j.Logger;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.*;

/**
 * @author <bxr4261>
 */
public abstract class BaseResource {

    private final static Logger logger = Logger.getLogger(BaseResource.class);

    private static IRMendDao dao;
    private static ContentRecommender contentRecommender;
    private static CFRecommender cfRecommender;
    private static Map<String, EntityManagerFactory> factory = new HashMap<String, EntityManagerFactory>();

    private static final String SERVICE_ACCOUNT_EMAIL =
            "777065455744-gqlc8dar2us2amkcig46lt0fffrarlqc@developer.gserviceaccount.com";

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/ping")
    public String ping() {
        return "pong from '"+getClass().getSimpleName() + "'";
    }

    public static IRMendDao getDao() {
        if (dao == null) {
            try {
                logger.info("Generating a new DAO instance");
                dao = new GCloudDao(getCredentials());
            } catch (GeneralSecurityException e) {
                logger.warn(e);
            } catch (IOException e) {
                logger.warn(e);
                e.printStackTrace();
            }
        }
        return dao;
    }

    public IRecommender getRecommender(RecommenderTypeEnum recommenderType) throws GeneralSecurityException, IOException {
        if(recommenderType == RecommenderTypeEnum.RANDOM) {
            recommenderType = RecommenderTypeEnum.getRandomRecommender();
        }
        if(recommenderType == RecommenderTypeEnum.CONTENT_BASED) {
            if (contentRecommender == null) {
                logger.info("Generating a new ContentRecommender instance");
                contentRecommender = new ContentRecommender(getDao());
            }
            return contentRecommender;
        }
        else if (recommenderType == RecommenderTypeEnum.COLLABORATIVE_FILTERING){
            if (cfRecommender == null) {
                logger.info("Generating a new CFRecommender instance");
                cfRecommender = new CFRecommender(getDao(), getCredentials());
            }
            return cfRecommender;
        }
        return null;
    }



    private static GoogleCredential getCredentials() throws GeneralSecurityException, IOException {
        HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        JacksonFactory jsonFactory = new JacksonFactory();
        File fCredPk = new File("conf/rmend-be.p12");

        GoogleCredential credential = new GoogleCredential.Builder().setTransport(httpTransport)
                .setJsonFactory(jsonFactory)
                .setServiceAccountId(SERVICE_ACCOUNT_EMAIL)
                .setServiceAccountScopes(DatastoreOptions.SCOPES)
                .setServiceAccountPrivateKeyFromP12File(fCredPk)
                .build();

        return credential;
    }

    public static EntityManager getEntityManager(String persistenceUnitName, Logger _logger) {
        if (!factory.containsKey(persistenceUnitName)) {
            _logger.info("Creating persistence unit: " + persistenceUnitName);
            factory.put(persistenceUnitName,
                    Persistence.createEntityManagerFactory(persistenceUnitName));
        }
        return factory.get(persistenceUnitName).createEntityManager();
    }

    public enum RecommenderTypeEnum {

        CONTENT_BASED,
        COLLABORATIVE_FILTERING,
        RANDOM;

        private RecommenderTypeEnum(){}

        public static RecommenderTypeEnum getRandomRecommender() {
            List<RecommenderTypeEnum> valuesAsList = Arrays.asList(values());
            Collections.shuffle(valuesAsList);
            return valuesAsList.get(0);
        }
    }
}
