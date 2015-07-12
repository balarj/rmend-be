package com.brajagopal.rmend.be.entities;

import com.brajagopal.rmend.app.beans.UserViewBean;
import com.google.appengine.datanucleus.annotations.Unindexed;
import org.joda.time.DateTime;

import javax.persistence.*;

/**
 * @author <bxr4261>
 */
@Entity
@Table(name = "USER_VIEWS")
public class ViewEntity {

    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Unindexed private Long id;
    private Long uid;
    private Long docNum;
    @Unindexed private String timestamp;
    @Id private String compositeKey;

    private ViewEntity(Long _id, Long _uid, Long _docNum) {
        this.id = _id;
        this.uid = _uid;
        this.docNum = _docNum;
        this.timestamp = DateTime.now().toString("YYYY-MM-dd HH:mm:ss");
        this.compositeKey = _uid + UserViewBean.COMPOSITE_KEY_SEPARATOR + _docNum;
    }

    private ViewEntity(Long _uid, Long _docNum) {
        this(0l, _uid, _docNum);
    }

    private ViewEntity(Long _uid, Long _docNum, String _timestamp) {
        this.uid = _uid;
        this.docNum = _docNum;
        this.timestamp = _timestamp;
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

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
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
                ", timestamp='" + timestamp + '\'' +
                '}';
    }
}
