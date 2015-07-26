package com.brajagopal.rmend.be.utils.bots;

import com.brajagopal.rmend.data.ResultsType;
import com.brajagopal.rmend.data.beans.DocumentBean;
import com.brajagopal.rmend.exception.DocumentNotFoundException;
import com.brajagopal.rmend.utils.DocumentManager;
import com.brajagopal.rmend.utils.RmendRequestAdapter;
import com.google.api.services.datastore.client.DatastoreException;
import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.http.HttpResponse;
import org.apache.http.nio.reactor.IOReactorException;
import org.apache.log4j.Logger;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InvalidClassException;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * @author <bxr4261>
 */
public class TopicBasedSelectorBot extends AutomationBaseBot implements IAutomationBot {

    private static Logger logger = Logger.getLogger(TopicBasedSelectorBot.class);
    private final RmendRequestAdapter requestAdapter;
    private ResultsType resultsType;

    private static final String ENDPOINT_TEMPLATE = "/v1/view/impression?referrer=AUTOBOT";

    @SuppressWarnings("unused")
    protected TopicBasedSelectorBot(String _targetHost, String _userId, String _topic) throws IOReactorException {
        this(_targetHost, _userId, _topic, ResultsType.RANDOM_10);
    }

    protected TopicBasedSelectorBot(String _targetHost, String _userId, String _topic, ResultsType _resultsType) throws IOReactorException {
        super(_userId, _topic);
        this.resultsType = _resultsType;
        requestAdapter = new RmendRequestAdapter(_targetHost, ENDPOINT_TEMPLATE);
    }

    public static void main(String[] args) {
        try {
            CommandLine cli = cliParser.parse(getCliOptions(), args);
            ResultsType resultsType;
            if (cli.getOptionValue('m') != null) {
                resultsType = ResultsType.valueOf(cli.getOptionValue('m'));
                if (resultsType == null) {
                    resultsType = ResultsType.RANDOM_10;
                }
            }
            else {
                resultsType = ResultsType.RANDOM_10;
            }

            final IAutomationBot autoBot = new TopicBasedSelectorBot(
                    cli.getOptionValue('h', "http://localhost:8088"),
                    cli.getOptionValue('u'),
                    cli.getOptionValue('t'),
                    resultsType
            );

            autoBot.start();
        } catch (IOReactorException e) {
            logger.error(e);
            e.printStackTrace();
        } catch (RuntimeException e) {
            e.printStackTrace();
            logger.error(e);
        } catch (ParseException e) {
            logger.error(e);
        }

    }

    @Override
    public void start() {
        try {
            Collection<Long> docNumbers = getDocumentsForTopic();
            if (docNumbers.isEmpty()) {
                logger.warn("No documents retrieved.. nothing to fetch!");
                return;
            }
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

        Collection<DocumentBean> documentBeans = documentManager.getContentByTopic(DocumentManager.makeTopicBean(topic), resultsType);
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
            List<HttpResponse> responses = requestAdapter.makeRequests(uid, _docNumbers);
            for (HttpResponse response : responses) {
                track(response);
            }
        } catch (InterruptedException e) {
            logger.warn(e);
        } catch (ExecutionException e) {
            e.printStackTrace();
            logger.warn(e);
        } catch (IOException e) {
            logger.warn(e);
        }
    }

    protected static Options getCliOptions() {
        final Options options = new Options();
        options.addOption("t", "topic", true, "Topic");
        options.addOption("u", "uid", true, "The UID associated with this request");
        options.addOption("h", "target", true, "The target hostname");
        options.addOption("m", "mode", true, "The ResultType");

        return options;
    }
}
