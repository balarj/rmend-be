package com.brajagopal.rmend.be.recommender;

import com.brajagopal.rmend.data.ResultsType;
import com.brajagopal.rmend.data.beans.DocumentBean;
import com.brajagopal.rmend.exception.DocumentNotFoundException;
import com.google.api.services.datastore.client.DatastoreException;

import java.util.Collection;

/**
 * @author <bxr4261>
 */
public interface IRecommender {

    public Collection<DocumentBean> getRecommendation(
            final long _documentNumber,
            ResultsType _resultsType) throws DatastoreException, DocumentNotFoundException;
}