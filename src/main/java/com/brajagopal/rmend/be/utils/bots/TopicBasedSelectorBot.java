package com.brajagopal.rmend.be.utils.bots;

import com.brajagopal.rmend.be.recommender.DocumentManager;
import com.brajagopal.rmend.data.ResultsType;
import com.brajagopal.rmend.data.beans.DocumentBean;
import com.brajagopal.rmend.exception.DocumentNotFoundException;
import com.brajagopal.rmend.utils.RmendRequestAdapter;
import com.google.api.services.datastore.client.DatastoreException;
import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import org.apache.http.HttpResponse;
import org.apache.http.nio.reactor.IOReactorException;
import org.apache.log4j.Logger;

import javax.annotation.Nullable;
import java.io.InvalidClassException;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * @author <bxr4261>
 */
public class TopicBasedSelectorBot extends AutomationBaseBot implements IAutomationBot {

    private static Logger logger = Logger.getLogger(TopicBasedSelectorBot.class);
    private final RmendRequestAdapter requestAdapter;

    private static final String ENDPOINT_TEMPLATE = "v1/view/impression";


    protected TopicBasedSelectorBot(String _targetHost, String _userId, String _topic) throws IOReactorException {
        super(_userId, _topic);
        requestAdapter = new RmendRequestAdapter(_targetHost, ENDPOINT_TEMPLATE);
    }

    public static void main(String[] args) {
        try {
            final IAutomationBot autoBot = new TopicBasedSelectorBot(
                    System.getProperties().getProperty("TARGET_HOST", "localhost:8080"),
                    System.getProperties().getProperty("UID"),
                    System.getProperties().getProperty("TOPIC")
            );
            autoBot.start();
        } catch (IOReactorException e) {
            logger.error(e);
            e.printStackTrace();
        } catch (RuntimeException e) {
            logger.error(e);
        }

    }

    @Override
    public void start() {
        try {
            Collection<Long> docNumbers = getDocumentsForTopic();
            makeRequests(docNumbers);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvalidClassException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (DocumentNotFoundException e) {
            e.printStackTrace();
        } catch (DatastoreException e) {
            e.printStackTrace();
        }
    }

    private Collection<Long> getDocumentsForTopic() throws IllegalAccessException, InvalidClassException, InstantiationException, DocumentNotFoundException, DatastoreException {
        DocumentManager documentManager = getDocumentManager();
        if (documentManager == null) {
            throw new NullPointerException("DocumentManager instance is NULL. Aborting...");
        }

        Collection<DocumentBean> documentBeans = documentManager.getContentByTopic(DocumentManager.makeTopicBean(topic), ResultsType.RANDOM_50);
        Collection<Long> docNumbers =
                Collections2.transform(documentBeans, new Function<DocumentBean, Long>() {

                    @Nullable
                    @Override
                    public Long apply(DocumentBean documentBean) {
                        return documentBean.getDocumentNumber();
                    }
                });

        return docNumbers;
    }

    private void makeRequests(Collection<Long> _docNumbers) {
        try {
            Future<List<HttpResponse>> responses = requestAdapter.makeRequests(uid, _docNumbers);
            for (HttpResponse response : responses.get()) {
                track(response);
            }
        } catch (InterruptedException e) {
            logger.warn(e);
        } catch (ExecutionException e) {
            logger.warn(e);
        }
    }
}
