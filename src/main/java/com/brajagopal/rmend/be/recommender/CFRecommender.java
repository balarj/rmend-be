package com.brajagopal.rmend.be.recommender;

import com.brajagopal.rmend.be.beans.RecResponseBean;
import com.brajagopal.rmend.be.model.GoogleDatastoreDataModel;
import com.brajagopal.rmend.dao.IRMendDao;
import com.brajagopal.rmend.data.ResultsType;
import com.brajagopal.rmend.data.beans.DocumentBean;
import com.brajagopal.rmend.data.beans.TopicBean;
import com.brajagopal.rmend.exception.DocumentNotFoundException;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.services.datastore.client.DatastoreException;
import org.apache.log4j.Logger;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.eval.RecommenderBuilder;
import org.apache.mahout.cf.taste.impl.recommender.GenericBooleanPrefItemBasedRecommender;
import org.apache.mahout.cf.taste.impl.similarity.TanimotoCoefficientSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.recommender.Recommender;
import org.apache.mahout.cf.taste.similarity.ItemSimilarity;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author <bxr4261>
 */
public class CFRecommender implements IRecommender {

    private final IRMendDao dao;
    private DataModel dataModel;
    private static Logger logger = Logger.getLogger(CFRecommender.class);

    public CFRecommender(IRMendDao dao, GoogleCredential credential) throws GeneralSecurityException, IOException {
        this.dao = dao;
        dataModel = new GoogleDatastoreDataModel(credential);
    }

    @Override
    public RecResponseBean getRecommendation(long _documentNumber, ResultsType _resultsType)
            throws DatastoreException, DocumentNotFoundException, TasteException {

        //RecommenderIRStatsEvaluator evaluator = new GenericRecommenderIRStatsEvaluator();
        RecommenderBuilderWrapper builder =
                new RecommenderBuilderWrapper(new TanimotoCoefficientSimilarity(dataModel));

        List<RecommendedItem> recommendations =
                builder.buildRecommender(dataModel).recommend(
                        _documentNumber, _resultsType.getDaoResultLimit());

        Collection<DocumentBean> results = new ArrayList<>(recommendations.size());
        for (RecommendedItem recommendedItem : recommendations) {
            try {
                results.add(dao.getDocument(recommendedItem.getItemID()));
            }
            catch (DocumentNotFoundException e) {
                logger.warn(e);
            }
        }

        RecResponseBean response = new RecResponseBean(
                results, builder.getSimilarityClass().getSimpleName());

        return response;
    }

    @Override
    public Collection<DocumentBean> getContentByTopic(TopicBean _topicBean, ResultsType _resultsType) throws DatastoreException, DocumentNotFoundException {
        return null;
    }

    static final class RecommenderBuilderWrapper implements RecommenderBuilder {

        private ItemSimilarity similarity;

        public RecommenderBuilderWrapper (ItemSimilarity instance) {
            this.similarity = instance;
        }

        @Override
        public Recommender buildRecommender(DataModel dataModel) throws TasteException {
            //similarity = new LogLikelihoodSimilarity(dataModel);
            return new GenericBooleanPrefItemBasedRecommender(dataModel, similarity);
        }

        public Class<? extends ItemSimilarity> getSimilarityClass() {
            return similarity.getClass();
        }
    }
}
