package com.brajagopal.rmend.be.beans;

import com.brajagopal.rmend.data.beans.DocumentBean;

import java.util.Collection;

/**
 * @author <bxr4261>
 */
public class RecResponseBean {

    public Collection<DocumentBean> results;
    public String similarityType;

    public RecResponseBean(Collection<DocumentBean> _docBean, String _similarityType) {
        this.results = _docBean;
        this.similarityType = _similarityType;
    }

    @Override
    public String toString() {
        return "CFRecommendResponse{" +
                "results=" + results +
                ", similarityType='" + similarityType + '\'' +
                '}';
    }

    public Collection<DocumentBean> getResults() {
        return results;
    }

    public String getSimilarityType() {
        return similarityType;
    }

    public boolean isEmpty() {
        return results.isEmpty();
    }

    public int size() {
        return results.size();
    }
}
