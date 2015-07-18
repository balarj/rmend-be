package com.brajagopal.rmend.be.recommender;

import com.brajagopal.rmend.be.beans.RecResponseBean;
import com.brajagopal.rmend.data.ResultsType;
import com.brajagopal.rmend.data.beans.DocumentBean;
import com.brajagopal.rmend.data.beans.TopicBean;
import com.brajagopal.rmend.exception.DocumentNotFoundException;
import com.google.api.services.datastore.client.DatastoreException;
import org.apache.mahout.cf.taste.common.TasteException;

import java.util.Collection;

/**
 * @author <bxr4261>
 */
public interface IRecommender {

    public RecResponseBean getRecommendation(
            final long _documentNumber,
            ResultsType _resultsType) throws DatastoreException, DocumentNotFoundException, TasteException;

    public Collection<DocumentBean> getContentByTopic(
            TopicBean _topicBean,
            ResultsType _resultsType) throws DatastoreException, DocumentNotFoundException;
}
