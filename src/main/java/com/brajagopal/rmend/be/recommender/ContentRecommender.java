package com.brajagopal.rmend.be.recommender;

import com.brajagopal.rmend.be.beans.RecResponseBean;
import com.brajagopal.rmend.dao.IRMendDao;
import com.brajagopal.rmend.data.ContentDictionary;
import com.brajagopal.rmend.data.ResultsType;
import com.brajagopal.rmend.data.beans.BaseContent;
import com.brajagopal.rmend.data.beans.DocumentBean;
import com.brajagopal.rmend.data.meta.DocumentMeta;
import com.brajagopal.rmend.exception.DocumentNotFoundException;
import com.google.api.services.datastore.client.DatastoreException;
import com.google.common.collect.TreeMultimap;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.Predicate;
import org.apache.log4j.Logger;
import org.apache.mahout.cf.taste.common.TasteException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.SortedSet;

/**
 * @author <bxr4261>
 */
public class ContentRecommender implements IRecommender {

    private IRMendDao dao;
    private static final Logger logger = Logger.getLogger(ContentRecommender.class);

    public ContentRecommender(IRMendDao _dao) {
        this.dao = _dao;
    }

    /**
     * Methods to retrieve Document that is similar to a given document
     *
     * @param _documentNumber
     * @param _resultsType
     * @return
     * @throws com.google.api.services.datastore.client.DatastoreException
     */
    @Override
    public RecResponseBean getRecommendation(
            final long _documentNumber,
            ResultsType _resultsType) throws DatastoreException, DocumentNotFoundException {

        DocumentBean baseDocument = dao.getDocument(_documentNumber);
        TreeMultimap<BaseContent.ContentType, BaseContent> relevantBeans = baseDocument.getRelevantBeans();
        SortedSet<BaseContent> topicRelatedBeans = relevantBeans.removeAll(BaseContent.ContentType.TOPICS);
        //TODO: Filter Topics that are the same as that of the base document

        Collection<String> entityIds = new ArrayList<String>();
        for (BaseContent contentBean : relevantBeans.values()) {
            entityIds.add(ContentDictionary.makeKeyFromBean(contentBean));
        }

        TreeMultimap<BaseContent.ContentType, DocumentMeta> entityResult = dao.getEntityMeta(entityIds, _resultsType);
        Collection<DocumentMeta> result = new ArrayList<DocumentMeta>();
        for (BaseContent.ContentType contentType : entityResult.keySet()) {
            Collection<DocumentMeta> docMetas = new ArrayList<DocumentMeta>(entityResult.get(contentType));
            CollectionUtils.filterInverse(docMetas, new Predicate<DocumentMeta>() {
                @Override
                public boolean evaluate(DocumentMeta documentMeta) {
                    return (documentMeta.getDocumentNumber() == _documentNumber);
                }
            });
            result.addAll(ResultsType.getResultsForCR(docMetas, _resultsType));
        }

        // Filter down the result (select randomly from the top results)
        result = ResultsType.getResultsForCR(result, ResultsType.RANDOM_10);
        result = ResultsType.getResultsForCR(result, _resultsType);

        Collection<DocumentBean> recommendedResults = new ArrayList<DocumentBean>();
        for (DocumentMeta docMeta : result) {
            recommendedResults.add(dao.getDocument(docMeta.getDocumentNumber()));
        }

        return new RecResponseBean(recommendedResults, "ContentBased");
    }

    @Override
    public RecResponseBean getItemSimilarity(long _documentNumber, ResultsType _resultsType) throws DatastoreException, DocumentNotFoundException, TasteException {
        throw new UnsupportedOperationException(
                "getItemSimilarity() not supported in ContentRecommender");
    }



}
