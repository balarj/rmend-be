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

    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Unindexed private Long id;
    private Long uid;
    private Long docNum;
    @Unindexed private long timestamp;
    @Unindexed private String impressionTime;
    @Id private String compositeKey;

    private ViewEntity(Long _id, Long _uid, Long _docNum) {
        DateTime dtNow = DateTime.now();
        this.id = _id;
        this.uid = _uid;
        this.docNum = _docNum;
        this.timestamp = dtNow.getMillis();
        this.impressionTime = dtNow.toString("YYYY-MM-dd HH:mm:ss");
        this.compositeKey = _uid + UserViewBean.COMPOSITE_KEY_SEPARATOR + _docNum;
    }

    private ViewEntity(Long _uid, Long _docNum) {
        this(0l, _uid, _docNum);
    }

    private ViewEntity(Long _uid, Long _docNum, String _timestamp) {
        DateTime dtNow = DateTime.now();
        this.uid = _uid;
        this.docNum = _docNum;
        this.timestamp = dtNow.getMillis();
        this.impressionTime = dtNow.toString("YYYY-MM-dd HH:mm:ss");
        this.compositeKey = _uid + UserViewBean.COMPOSITE_KEY_SEPARATOR + _docNum;
    }

    public ViewEntity() {}

    public static ViewEntity createInstance(UserViewBean userViewBean) {
        return new ViewEntity(
                userViewBean.getUid(),
                userViewBean.getDocNum(),
                userViewBean.getUpdateTS().toString("YYYY-MM-dd HH:mm:ss")
        );
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    @Override
    public String toString() {
        return "ViewEntity{" +
                "id=" + id +
                ", uid=" + uid +
                ", docNum=" + docNum +
                ", impressionTime='" + impressionTime + '\'' +
                '}';
    }
}
