package com.brajagopal.rmend.be.entities;

import com.brajagopal.rmend.app.beans.UserViewBean;
import com.google.appengine.datanucleus.annotations.Unindexed;
import org.joda.time.DateTime;

import javax.persistence.*;

/**
 * @author <bxr4261>
 */
@Entity
@Table(name = "IMPRESSIONS")
public class ViewEntity {

    private Long uid;
    private Long docNum;
    @Unindexed private long timestamp;
    @Unindexed private String impressionTime;
    @Id private String compositeKey;
    @Unindexed private String recommendationType;

    private ViewEntity(Long _uid, Long _docNum) {
        DateTime dtNow = DateTime.now();
        this.uid = _uid;
        this.docNum = _docNum;
        this.timestamp = dtNow.getMillis();
        this.impressionTime = dtNow.toString("YYYY-MM-dd HH:mm:ss");
        this.compositeKey = _uid + UserViewBean.COMPOSITE_KEY_SEPARATOR + _docNum;
    }

    private ViewEntity(Long _uid, Long _docNum, String _recType) {
        DateTime dtNow = DateTime.now();
        this.uid = _uid;
        this.docNum = _docNum;
        this.timestamp = dtNow.getMillis();
        this.impressionTime = dtNow.toString("YYYY-MM-dd HH:mm:ss");
        this.compositeKey = _uid + UserViewBean.COMPOSITE_KEY_SEPARATOR + _docNum;
        this.recommendationType = _recType;
    }

    public ViewEntity() {}

    public static ViewEntity createInstance(UserViewBean userViewBean) {
        return new ViewEntity(
                userViewBean.getUid(),
                userViewBean.getDocNum(),
                userViewBean.getRecType()
        );
    }

    public Long getUid() {
        return uid;
    }

    public void setUid(Long uid) {
        this.uid = uid;
    }

    public Long getDocNum() {
        return docNum;
    }

    public void setDocNum(Long docNum) {
        this.docNum = docNum;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public String getImpressionTime() {
        return impressionTime;
    }

    public void setImpressionTime(String impressionTime) {
        this.impressionTime = impressionTime;
    }

    public String getCompositeKey() {
        return compositeKey;
    }

    public void setCompositeKey(String compositeKey) {
        this.compositeKey = compositeKey;
    }

    public String getRecommendationType() {
        return recommendationType;
    }

    public void setRecommendationType(String recommendationType) {
        this.recommendationType = recommendationType;
    }

    @Override
    public String toString() {
        return "ViewEntity{" +
                "uid=" + uid +
                ", docNum=" + docNum +
                ", impressionTime='" + impressionTime + '\'' +
                ", recommendationType='" + recommendationType + '\'' +
                '}';
    }
}
