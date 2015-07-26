package com.brajagopal.rmend.be.recommender;

import com.brajagopal.rmend.be.beans.RecResponseBean;
import com.brajagopal.rmend.be.model.GoogleDatastoreDataModel;
import com.brajagopal.rmend.dao.IRMendDao;
import com.brajagopal.rmend.data.ResultsType;
import com.brajagopal.rmend.data.beans.DocumentBean;
import com.brajagopal.rmend.exception.DocumentNotFoundException;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.services.datastore.client.DatastoreException;
import org.apache.log4j.Logger;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.eval.RecommenderBuilder;
import org.apache.mahout.cf.taste.impl.neighborhood.ThresholdUserNeighborhood;
import org.apache.mahout.cf.taste.impl.recommender.GenericBooleanPrefItemBasedRecommender;
import org.apache.mahout.cf.taste.impl.recommender.GenericBooleanPrefUserBasedRecommender;
import org.apache.mahout.cf.taste.impl.similarity.PearsonCorrelationSimilarity;
import org.apache.mahout.cf.taste.impl.similarity.TanimotoCoefficientSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.neighborhood.UserNeighborhood;
import org.apache.mahout.cf.taste.recommender.ItemBasedRecommender;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.recommender.UserBasedRecommender;
import org.apache.mahout.cf.taste.similarity.ItemSimilarity;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;

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
    public RecResponseBean getRecommendation(long _userId, ResultsType _resultsType)
            throws DatastoreException, DocumentNotFoundException, TasteException {

        //RecommenderIRStatsEvaluator evaluator = new GenericRecommenderIRStatsEvaluator();
        RecommendationBuilderWrapper builder =
                new RecommendationBuilderWrapper(
                        new PearsonCorrelationSimilarity(dataModel));

        List<RecommendedItem> recommendations =
                builder.buildRecommender(dataModel).recommend(
                        _userId, _resultsType.getDaoResultLimit());

        Collection<DocumentBean> results = new ArrayList<>(recommendations.size());

        for (RecommendedItem recommendedItem : recommendations) {
            try {
                results.add(dao.getDocument(recommendedItem.getItemID()));
            }
            catch (DocumentNotFoundException e) {
                logger.warn(e);
            }
        }

        // Filter down the result (select randomly from the top results)
        results = ResultsType.getResultsForCF(results, ResultsType.RANDOM_10);
        results = ResultsType.getResultsForCF(results, _resultsType);

        RecResponseBean response = new RecResponseBean(
                results, builder.getSimilarityClass().getSimpleName());

        return response;
    }

    @Override
    public RecResponseBean getItemSimilarity(long _documentNumber, ResultsType _resultsType)
            throws DatastoreException, DocumentNotFoundException, TasteException {

        SimilarityBuilderWrapper builder =
                new SimilarityBuilderWrapper(
                        new TanimotoCoefficientSimilarity(dataModel));

        List<RecommendedItem> recommendations =
                builder.buildRecommender(dataModel).mostSimilarItems(
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

        // Filter down the result (select randomly from the top results)
        results = ResultsType.getResultsForCF(results, ResultsType.RANDOM_10);
        results = ResultsType.getResultsForCF(results, _resultsType);

        RecResponseBean response = new RecResponseBean(
                results, builder.getSimilarityClass().getSimpleName());

        return response;
    }

    static final class SimilarityBuilderWrapper implements RecommenderBuilder {

        private ItemSimilarity similarity;

        public SimilarityBuilderWrapper(ItemSimilarity instance) {
            this.similarity = instance;
        }

        @Override
        public ItemBasedRecommender buildRecommender(DataModel dataModel) throws TasteException {
            return new GenericBooleanPrefItemBasedRecommender(dataModel, similarity);
        }

        public Class<? extends ItemSimilarity> getSimilarityClass() {
            return similarity.getClass();
        }
    }

    static final class RecommendationBuilderWrapper implements RecommenderBuilder {

        private UserSimilarity similarity;

        public RecommendationBuilderWrapper(UserSimilarity instance) {
            this.similarity = instance;
        }

        @Override
        public UserBasedRecommender buildRecommender(DataModel dataModel) throws TasteException {

            UserNeighborhood neighborhood =
                    new ThresholdUserNeighborhood(
                            0.1, new PearsonCorrelationSimilarity(dataModel), dataModel);

            return new GenericBooleanPrefUserBasedRecommender(
                    dataModel,
                    neighborhood,
                    similarity);
        }

        public Class<? extends UserSimilarity> getSimilarityClass() {
            return similarity.getClass();
        }
    }
}
