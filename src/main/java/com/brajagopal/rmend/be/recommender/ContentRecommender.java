package com.brajagopal.rmend.be.recommender;

import com.brajagopal.rmend.dao.IRMendDao;
import com.brajagopal.rmend.data.ContentDictionary;
import com.brajagopal.rmend.data.ResultsType;
import com.brajagopal.rmend.data.beans.BaseContent;
import com.brajagopal.rmend.data.beans.DocumentBean;
import com.brajagopal.rmend.data.beans.TopicBean;
import com.brajagopal.rmend.data.meta.DocumentMeta;
import com.brajagopal.rmend.exception.DocumentNotFoundException;
import com.google.api.services.datastore.client.DatastoreException;
import com.google.common.collect.TreeMultimap;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.Predicate;
import org.apache.log4j.Logger;

import java.io.InvalidClassException;
import java.util.*;

/**
 * @author <bxr4261>
 */
public class ContentRecommender {

    private IRMendDao dao;
    private static final Logger logger = Logger.getLogger(ContentRecommender.class);

    public ContentRecommender(IRMendDao _dao) {
        this.dao = _dao;
    }

    /**
     * Method to retrieve Document (content), filtered by topic
     *
     * @param _topicBean
     * @param _resultsType
     * @return
     * @throws com.google.api.services.datastore.client.DatastoreException
     */
    public Collection<DocumentBean> getContentByTopic(TopicBean _topicBean, ResultsType _resultsType) throws DatastoreException, DocumentNotFoundException {
        Collection<DocumentBean> docBeans = new ArrayList<DocumentBean>();
        Collection<DocumentMeta> docMetaCollection = getDocumentMeta(ContentDictionary.makeKeyFromBean(_topicBean), _resultsType);
        for (DocumentMeta documentMeta : docMetaCollection) {
            docBeans.add(dao.getDocument(documentMeta.getDocumentNumber()));
        }
        return docBeans;
    }

    /**
     * Methods to retrieve Document that is similar to a given document
     *
     * @param _documentNumber
     * @param _resultsType
     * @return
     * @throws com.google.api.services.datastore.client.DatastoreException
     */
    public Collection<DocumentBean> getSimilarContent(final long _documentNumber, ResultsType _resultsType) throws DatastoreException, DocumentNotFoundException {
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
            result.addAll(ResultsType.getResults(docMetas, _resultsType));
        }

        // Filter down the result (select randomly from the top results)
        result = ResultsType.getResults(result, ResultsType.RANDOM_10);
        result = ResultsType.getResults(result, _resultsType);

        Collection<DocumentBean> recommendedResults = new ArrayList<DocumentBean>();
        for (DocumentMeta docMeta : result) {
            recommendedResults.add(dao.getDocument(docMeta.getDocumentNumber()));
        }

        return recommendedResults;
    }



    public static TopicBean makeTopicBean(String _topic) throws IllegalAccessException, InvalidClassException, InstantiationException {
        Map<String, String> beanValues = new HashMap<String, String>();
        beanValues.put("contentType", BaseContent.ContentType.TOPICS.toString());
        beanValues.put("name", _topic);
        BaseContent retVal = BaseContent.getChildInstance(beanValues);
        if (retVal != null && retVal instanceof TopicBean) {
            return (TopicBean) retVal;
        }
        else {
            throw new RuntimeException("Unable to generate a TopicBean for the topic: "+_topic);
        }
    }

    protected Collection<DocumentMeta> getDocumentMeta(String _metaIdentifier,  ResultsType resultsType) throws DatastoreException {
        Collection<DocumentMeta> entityValues = dao.getEntityMeta(_metaIdentifier);
        return ResultsType.getResults(entityValues, resultsType);
    }
}
