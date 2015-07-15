package com.brajagopal.rmend.be.model;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.services.datastore.DatastoreV1.*;
import com.google.api.services.datastore.client.Datastore;
import com.google.api.services.datastore.client.DatastoreException;
import com.google.api.services.datastore.client.DatastoreFactory;
import com.google.api.services.datastore.client.DatastoreHelper;
import org.apache.log4j.Logger;
import org.apache.mahout.cf.taste.common.NoSuchItemException;
import org.apache.mahout.cf.taste.common.NoSuchUserException;
import org.apache.mahout.cf.taste.common.Refreshable;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.common.Cache;
import org.apache.mahout.cf.taste.impl.common.FastIDSet;
import org.apache.mahout.cf.taste.impl.common.LongPrimitiveIterator;
import org.apache.mahout.cf.taste.impl.common.Retriever;
import org.apache.mahout.cf.taste.impl.model.GenericUserPreferenceArray;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.model.PreferenceArray;

import java.io.Closeable;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author <bxr4261>
 */
public final class GoogleDatastoreDataModel implements DataModel, Closeable {

    private static final Logger logger = Logger.getLogger(GoogleDatastoreDataModel.class);

    private final Datastore datastore;
    private static final String DEFAULT_DATASET = "recommend";
    private static final String DEFAULT_IMPRESSIONS_KIND_NAME = "IMPRESSIONS";
    private static final String DEFAULT_IMPRESSIONS_ITEMS_KIND_NAME = "IMPRESSIONS_ITEMS";
    private static final String DEFAULT_IMPRESSIONS_USER_KIND_NAME = "IMPRESSIONS_USERS";


    private static final String USER_ID_COLUMN = "uid";
    private static final String ITEM_ID_COLUMN = "docNum";
    private static final String TIMESTAMP_COLUMN = "timestamp";

    private int writeBatchSize;
    private int readBatchSize;

    private final Cache<Long,PreferenceArray> userCache;
    private final Cache<Long,PreferenceArray> itemCache;
    private final Cache<Long,FastIDSet> itemIDsFromUserCache;
    private final Cache<Long,FastIDSet> userIDsFromItemCache;
    private final AtomicReference<Integer> userCountCache;
    private final AtomicReference<Integer> itemCountCache;

    /**
     *
     * @throws GeneralSecurityException
     * @throws IOException
     */
    public GoogleDatastoreDataModel() throws GeneralSecurityException, IOException {
        String defaultDataSet = System.getProperties().getProperty("com.google.appengine.application.id");
        datastore = DatastoreFactory.get()
                .create(DatastoreHelper.getOptionsFromEnv()
                                .dataset(defaultDataSet)
                                .build()
                );

        userCache = new Cache<>(new UserPrefArrayRetriever(), 1 << 20);
        itemCache = new Cache<>(new ItemPrefArrayRetriever(), 1 << 20);
        itemIDsFromUserCache = new Cache<>(new ItemIDsFromUserRetriever(), 1 << 20);
        userIDsFromItemCache = new Cache<>(new UserIDsFromItemRetriever(), 1 << 20);
        userCountCache = new AtomicReference<>(null);
        itemCountCache = new AtomicReference<>(null);
    }

    /**
     * Dataset should come from the environment variables.
     * Available options are:
     *  - Inject using a POM file
     *  - If running on app-engine, Inject using appengine-web.xml
     *
     * @param credential
     * @throws GeneralSecurityException
     * @throws IOException
     */
    public GoogleDatastoreDataModel(GoogleCredential credential) throws GeneralSecurityException, IOException {

        String defaultDataSet = System.getProperties().getProperty("com.google.appengine.application.id");
        datastore = DatastoreFactory.get()
                .create(DatastoreHelper.getOptionsFromEnv()
                                .dataset(defaultDataSet)
                                .credential(credential)
                                .build()
                );

        userCache = new Cache<>(new UserPrefArrayRetriever(), 1 << 20);
        itemCache = new Cache<>(new ItemPrefArrayRetriever(), 1 << 20);
        itemIDsFromUserCache = new Cache<>(new ItemIDsFromUserRetriever(), 1 << 20);
        userIDsFromItemCache = new Cache<>(new UserIDsFromItemRetriever(), 1 << 20);
        userCountCache = new AtomicReference<>(null);
        itemCountCache = new AtomicReference<>(null);
    }

    /**
     *
     * @param credential
     * @param dataset
     * @throws GeneralSecurityException
     * @throws IOException
     */
    public GoogleDatastoreDataModel(GoogleCredential credential, String dataset) throws GeneralSecurityException, IOException {
        datastore = DatastoreFactory.get().create(
                DatastoreHelper.getOptionsFromEnv().credential(credential).dataset(dataset).build());

        userCache = new Cache<>(new UserPrefArrayRetriever(), 1 << 20);
        itemCache = new Cache<>(new ItemPrefArrayRetriever(), 1 << 20);
        itemIDsFromUserCache = new Cache<>(new ItemIDsFromUserRetriever(), 1 << 20);
        userIDsFromItemCache = new Cache<>(new UserIDsFromItemRetriever(), 1 << 20);
        userCountCache = new AtomicReference<>(null);
        itemCountCache = new AtomicReference<>(null);
    }

    /**
     * Closes this stream and releases any system resources associated
     * with it. If the stream is already closed then invoking this
     * method has no effect.
     *
     * @throws IOException if an I/O error occurs
     */
    @Override
    public void close() throws IOException {
        // Nothing to do!
    }

    @Override
    public LongPrimitiveIterator getUserIDs() throws TasteException {
        FastIDSet userIDs = new FastIDSet();
        try {
            Query.Builder query = Query.newBuilder();
            query.addKindBuilder().setName(DEFAULT_IMPRESSIONS_USER_KIND_NAME);
            List<Entity> results = runQuery(query.build(), "getUserIDs");
            logger.info(results.size());
            for (Entity entity : results) {
                logger.info(DatastoreHelper.getPropertyMap(entity));
                userIDs.add(
                        DatastoreHelper.getLong(
                                DatastoreHelper.getPropertyMap(entity).get(USER_ID_COLUMN)
                        )
                );
            }
        } catch (DatastoreException e) {
            throw new TasteException(e);
        }
        return userIDs.iterator();
    }

    @Override
    public PreferenceArray getPreferencesFromUser(long userID) throws TasteException {
        return userCache.get(userID);
    }

    @Override
    public FastIDSet getItemIDsFromUser(long userID) throws TasteException {
        return itemIDsFromUserCache.get(userID);
    }

    @Override
    public LongPrimitiveIterator getItemIDs() throws TasteException {
        FastIDSet itemIDs = new FastIDSet();
        try {
            Query.Builder query = Query.newBuilder();
            query.setLimit(Integer.MAX_VALUE);
            query.addKindBuilder().setName(DEFAULT_IMPRESSIONS_KIND_NAME);
            List<Entity> results = runQuery(query.build(), "getItemIDs()");
            for (Entity entity : results) {
                itemIDs.add(
                        DatastoreHelper.getLong(
                                DatastoreHelper.getPropertyMap(entity).get(ITEM_ID_COLUMN)
                        )
                );
            }
        } catch (DatastoreException e) {
            throw new TasteException(e);
        }
        return itemIDs.iterator();
    }

    @Override
    public PreferenceArray getPreferencesForItem(long itemID) throws TasteException {
        return itemCache.get(itemID);
    }

    @Override
    public Float getPreferenceValue(long userID, long itemID) throws TasteException {
        try {
            Query.Builder query = Query.newBuilder();
            query.setLimit(Integer.MAX_VALUE);
            query.addKindBuilder().setName(DEFAULT_IMPRESSIONS_KIND_NAME);
            // Filter by userId
            query.setFilter(DatastoreHelper.makeFilter(
                    USER_ID_COLUMN,
                    PropertyFilter.Operator.EQUAL,
                    DatastoreHelper.makeValue(userID)
            ));
            query.setFilter(DatastoreHelper.makeFilter(
                    ITEM_ID_COLUMN,
                    PropertyFilter.Operator.EQUAL,
                    DatastoreHelper.makeValue(userID)
            ));
            List<Entity> results = runQuery(query.build(), "getPreferenceValue()");
            if(results.isEmpty()) {
                return null;
            }
            return 1f; // Boolean/Binary preference values
        } catch (DatastoreException e) {
            throw new TasteException(e);
        }
    }

    @Override
    public Long getPreferenceTime(long userID, long itemID) throws TasteException {
        try {
            Query.Builder query = Query.newBuilder();
            query.setLimit(Integer.MAX_VALUE);
            query.addKindBuilder().setName(DEFAULT_IMPRESSIONS_KIND_NAME);
            // Filter by userId
            query.setFilter(DatastoreHelper.makeFilter(
                    USER_ID_COLUMN,
                    PropertyFilter.Operator.EQUAL,
                    DatastoreHelper.makeValue(userID)
            ));
            query.setFilter(DatastoreHelper.makeFilter(
                    ITEM_ID_COLUMN,
                    PropertyFilter.Operator.EQUAL,
                    DatastoreHelper.makeValue(userID)
            ));
            List<Entity> results = runQuery(query.build(), "getPreferenceTime()");
            if(results.isEmpty()) {
                return null;
            }
            return DatastoreHelper.getLong(DatastoreHelper.getPropertyMap(results.get(0)).get(TIMESTAMP_COLUMN));
        } catch (DatastoreException e) {
            throw new TasteException(e);
        }
    }

    @Override
    public int getNumItems() throws TasteException {
        Integer itemCount = itemCountCache.get();
        logger.info(itemCount);
        if (itemCount == null) {
            try {
                Query.Builder query = Query.newBuilder();
                query.addKindBuilder().setName(DEFAULT_IMPRESSIONS_ITEMS_KIND_NAME);
                query.addProjection(PropertyExpression.newBuilder().setProperty(
                        PropertyReference.newBuilder().setName("__key__")));
                List<Key> results = runProjectionQuery(query.build(), "getNumItems()");
                itemCount = results.size();
                itemCountCache.set(itemCount);
            } catch (DatastoreException e) {
                e.printStackTrace();
                throw new TasteException(e);
            }
        }
        logger.info("itemCount: "+itemCount);
        return itemCount;
    }

    @Override
    public int getNumUsers() throws TasteException {
        Integer userCount = userCountCache.get();
        if (userCount == null) {
            try {
                Query.Builder query = Query.newBuilder();
                query.setLimit(Integer.MAX_VALUE);
                query.addKindBuilder().setName(DEFAULT_IMPRESSIONS_USER_KIND_NAME);
                List<Entity> results = runQuery(query.build(), "getNumUsers()");
                userCount = results.size();
                userCountCache.set(userCount);
            } catch (DatastoreException e) {
                throw new TasteException(e);
            }
        }
        logger.info("userCount: "+userCount);
        return userCount;
    }

    @Override
    public int getNumUsersWithPreferenceFor(long itemID) throws TasteException {
        return userIDsFromItemCache.get(itemID).size();
    }

    @Override
    public int getNumUsersWithPreferenceFor(long itemID1, long itemID2) throws TasteException {
        FastIDSet userIDs1 = userIDsFromItemCache.get(itemID1);
        FastIDSet userIDs2 = userIDsFromItemCache.get(itemID2);
        return userIDs1.size() < userIDs2.size()
                ? userIDs2.intersectionSize(userIDs1)
                : userIDs1.intersectionSize(userIDs2);
    }

    @Override
    public void setPreference(long l, long l1, float v) throws TasteException {
        //TODO: Implement in the future. We don't use it for now
    }

    @Override
    public void removePreference(long l, long l1) throws TasteException {
        //TODO: Implement in the future. We don't use it for now
    }

    @Override
    public boolean hasPreferenceValues() {
        return true;
    }

    @Override
    public float getMaxPreference() {
        return Float.NaN;
    }

    @Override
    public float getMinPreference() {
        return Float.NaN;
    }

    @Override
    public void refresh(Collection<Refreshable> collection) {
        userCache.clear();
        itemCache.clear();
        userIDsFromItemCache.clear();
        itemIDsFromUserCache.clear();
        userCountCache.set(null);
        itemCountCache.set(null);
    }

    private List<Entity> runQuery(Query query, String _callingMethod) throws DatastoreException {
        RunQueryRequest.Builder request = RunQueryRequest.newBuilder();
        request.setQuery(query);
        logger.info(_callingMethod + " : " + query);
        RunQueryResponse response = datastore.runQuery(request.build());

        if (response.getBatch().getMoreResults() == QueryResultBatch.MoreResultsType.NOT_FINISHED) {
            System.err.println("WARNING: partial results\n");
        }
        List<EntityResult> results = response.getBatch().getEntityResultList();
        List<Entity> entities = new ArrayList<>(results.size());
        for (EntityResult result : results) {
            entities.add(result.getEntity());
        }
        return entities;
    }

    private List<Key> runProjectionQuery(Query query, String _callingMethod) throws DatastoreException {
        RunQueryRequest.Builder request = RunQueryRequest.newBuilder();
        request.setQuery(query);
        logger.info(_callingMethod+" : "+query);
        RunQueryResponse response = datastore.runQuery(request.build());

        // TODO:
        // If you see this message; it means that you will have
        // to set the limit to INTEGER.MAX in the calling method.
        if (response.getBatch().getMoreResults() == QueryResultBatch.MoreResultsType.NOT_FINISHED) {
            System.err.println("WARNING: partial results\n");
        }
        List<EntityResult> results = response.getBatch().getEntityResultList();
        List<Key> entities = new ArrayList<>(results.size());
        for (EntityResult result : results) {
            entities.add(result.getEntity().getKey());
        }
        return entities;
    }

    private final class UserPrefArrayRetriever implements Retriever<Long, PreferenceArray> {

        @Override
        public PreferenceArray get(Long userID) throws TasteException {
            try {
                List<Entity> results = getItemsForUser(userID);
                if(results == null || results.isEmpty()) {
                    throw new NoSuchUserException(userID);
                }
                int i = 0;
                PreferenceArray prefs = new GenericUserPreferenceArray(results.size());
                prefs.setUserID(0, userID);
                for (Entity entity : results) {
                    prefs.setItemID(
                            i,
                            DatastoreHelper.getLong(
                                    DatastoreHelper.getPropertyMap(entity).get(ITEM_ID_COLUMN)
                            )
                    );
                    prefs.setValue(i, 1f);
                    i++;
                }
                return prefs;
            } catch (DatastoreException e) {
                throw new TasteException(e);
            }
        }
    }

    private final class ItemPrefArrayRetriever implements Retriever<Long, PreferenceArray> {
        @Override
        public PreferenceArray get(Long itemID) throws TasteException {
            try {
                List<Entity> results = getUsersForItem(itemID);
                if(results == null || results.isEmpty()) {
                    throw new NoSuchItemException(itemID);
                }
                int i = 0;
                PreferenceArray prefs = new GenericUserPreferenceArray(results.size());
                prefs.setItemID(0, itemID);
                for (Entity entity : results) {
                    prefs.setUserID(
                            i,
                            DatastoreHelper.getLong(
                                    DatastoreHelper.getPropertyMap(entity).get(USER_ID_COLUMN)
                            )
                    );
                    prefs.setValue(i, 1f);
                    i++;
                }
                return prefs;
            } catch (DatastoreException e) {
                throw new TasteException(e);
            }
        }
    }

    private final class UserIDsFromItemRetriever implements Retriever<Long, FastIDSet> {
        @Override
        public FastIDSet get(Long itemID) throws TasteException {
            try {
                List<Entity> results = getUsersForItem(itemID);
                if(results == null || results.isEmpty()) {
                    throw new NoSuchItemException(itemID);
                }

                FastIDSet userIDs = new FastIDSet(results.size());
                for (Entity entity : results) {
                    userIDs.add(
                            DatastoreHelper.getLong(
                                    DatastoreHelper.getPropertyMap(entity).get(USER_ID_COLUMN)
                            )
                    );
                }
                return userIDs;
            } catch (DatastoreException e) {
                throw new TasteException(e);
            }
        }
    }

    private final class ItemIDsFromUserRetriever implements Retriever<Long, FastIDSet> {
        @Override
        public FastIDSet get(Long userID) throws TasteException {
            try {
                List<Entity> results = getItemsForUser(userID);
                if(results == null || results.isEmpty()) {
                    throw new NoSuchUserException(userID);
                }

                FastIDSet itemIDs = new FastIDSet(results.size());
                for (Entity entity : results) {
                    itemIDs.add(
                            DatastoreHelper.getLong(
                                    DatastoreHelper.getPropertyMap(entity).get(ITEM_ID_COLUMN)
                            )
                    );
                }
                return itemIDs;
            } catch (DatastoreException e) {
                throw new TasteException(e);
            }
        }
    }

    private final List<Entity> getItemsForUser(Long userID) throws DatastoreException {
        Query.Builder query = Query.newBuilder();
        query.setLimit(Integer.MAX_VALUE);
        query.addKindBuilder().setName(DEFAULT_IMPRESSIONS_KIND_NAME);
        // Filter by userId
        query.setFilter(DatastoreHelper.makeFilter(
                USER_ID_COLUMN,
                PropertyFilter.Operator.EQUAL,
                DatastoreHelper.makeValue(userID)
        ));
        return runQuery(query.build(), "getItemsForUser()");
    }

    private final List<Entity> getUsersForItem(Long itemID) throws DatastoreException {
        Query.Builder query = Query.newBuilder();
        query.setLimit(Integer.MAX_VALUE);
        query.addKindBuilder().setName(DEFAULT_IMPRESSIONS_KIND_NAME);
        // Filter by userId
        query.setFilter(DatastoreHelper.makeFilter(
                ITEM_ID_COLUMN,
                PropertyFilter.Operator.EQUAL,
                DatastoreHelper.makeValue(itemID)
        ));
        return runQuery(query.build(), "getUsersForItem()");
    }
}
