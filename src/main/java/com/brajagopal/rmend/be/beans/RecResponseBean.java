package com.brajagopal.rmend.be.beans;

import com.brajagopal.rmend.data.beans.DocumentBean;

import java.util.Collection;

/**
 * @author <bxr4261>
 */
public class RecResponseBean {

    public Collection<DocumentBean> results;
    public String similarityType;
    public String similarityClass;

    public RecResponseBean(Collection<DocumentBean> _docBean, String _similarityType) {
        this.results = _docBean;
        this.similarityType = _similarityType;
    }

    @Override
    public String toString() {
        return "RecResponseBean{" +
                "results=" + results +
                ", similarityType='" + similarityType + '\'' +
                ", similarityClass='" + similarityClass + '\'' +
                '}';
    }

    public RecResponseBean(Collection<DocumentBean> _docBean, String _similarityType, String _similarityClass) {
        this(_docBean, _similarityType);
        this.similarityClass = _similarityClass;
    }

    public Collection<DocumentBean> getResults() {
        return results;
    }

    public String getSimilarityType() {
        return similarityType;
    }

    public String getSimilarityClass() {
        return similarityClass;
    }

    public boolean isEmpty() {
        return results.isEmpty();
    }

    public int size() {
        return results.size();
    }
}
