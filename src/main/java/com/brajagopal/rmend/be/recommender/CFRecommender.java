package com.brajagopal.rmend.be.recommender;

import com.brajagopal.rmend.be.model.GoogleDatastoreDataModel;
import com.brajagopal.rmend.data.ResultsType;
import com.brajagopal.rmend.data.beans.DocumentBean;
import com.brajagopal.rmend.data.beans.TopicBean;
import com.brajagopal.rmend.exception.DocumentNotFoundException;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.services.datastore.client.DatastoreException;
import org.apache.log4j.Logger;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.eval.IRStatistics;
import org.apache.mahout.cf.taste.eval.RecommenderBuilder;
import org.apache.mahout.cf.taste.eval.RecommenderIRStatsEvaluator;
import org.apache.mahout.cf.taste.impl.eval.GenericRecommenderIRStatsEvaluator;
import org.apache.mahout.cf.taste.impl.recommender.GenericItemBasedRecommender;
import org.apache.mahout.cf.taste.impl.similarity.PearsonCorrelationSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.recommender.Recommender;
import org.apache.mahout.cf.taste.similarity.ItemSimilarity;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collection;
import java.util.List;

/**
 * @author <bxr4261>
 */
public class CFRecommender implements IRecommender {

    private DataModel dataModel;
    private static Logger logger = Logger.getLogger(CFRecommender.class);

    public CFRecommender(GoogleCredential credential) throws GeneralSecurityException, IOException {
        dataModel = new GoogleDatastoreDataModel(credential);
    }


    @Override
    public Collection<DocumentBean> getRecommendation(long _documentNumber, ResultsType _resultsType)
            throws DatastoreException, DocumentNotFoundException, TasteException {

        logger.info(_documentNumber);
        RecommenderIRStatsEvaluator evaluator = new GenericRecommenderIRStatsEvaluator();
        RecommenderBuilder builder = new RecommenderBuilder() {
            @Override
            public Recommender buildRecommender(DataModel dataModel) throws TasteException {
                ItemSimilarity similarity = new PearsonCorrelationSimilarity(dataModel);
                return new GenericItemBasedRecommender(dataModel, similarity);
            }
        };

        IRStatistics stats = evaluator.evaluate(
                builder,
                null,
                dataModel,
                null,
                5,
                GenericRecommenderIRStatsEvaluator.CHOOSE_THRESHOLD,
                1
        );

        List<RecommendedItem> rec =
                builder.buildRecommender(dataModel).recommend(
                        _documentNumber, _resultsType.getDaoResultLimit());

        logger.info(rec);
        return null;
    }

    @Override
    public Collection<DocumentBean> getContentByTopic(TopicBean _topicBean, ResultsType _resultsType) throws DatastoreException, DocumentNotFoundException {
        return null;
    }
}
