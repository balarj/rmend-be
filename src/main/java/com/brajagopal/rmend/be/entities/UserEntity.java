package com.brajagopal.rmend.be.entities;

import com.brajagopal.rmend.app.beans.UserBean;
import com.google.appengine.datanucleus.annotations.Unindexed;
import org.joda.time.DateTime;

import javax.persistence.*;

/**
 * @author <bxr4261>
 */
@Entity
@Table(name = "USER")
public class UserEntity {

    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Unindexed private Long uid;
    @Id private String userName;
    private String uuid;
    @Unindexed private String createdDate;

    private UserEntity(Long _uid, String _userName, String _uuid) {
        this.uid = _uid;
        this.userName = _userName;
        this.uuid = _uuid;
        this.createdDate = DateTime.now().toString("YYYY-MM-dd HH:mm:ss");
    }

    private UserEntity(String _userName, String _uuid) {
        this(0l, _userName, _uuid);
    }

    public UserEntity() {}

    public static UserEntity createInstance(UserBean _userBean) {
        return new UserEntity(_userBean.getUserName(), _userBean.getUuid());
    }

    public long getUid() {
        return uid;
    }

    public String getUserName() {
        return userName;
    }

    public void setUid(Long uid) {
        this.uid = uid;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(String createdDate) {
        this.createdDate = createdDate;
    }

    @Override
    public String toString() {
        return "UserEntity{" +
                "uid=" + uid +
                ", userName='" + userName + '\'' +
                ", uuid='" + uuid + '\'' +
                ", createdDate=" + createdDate +
                '}';
    }

    public UserBean getUserBean() {
        return UserBean.load(getUid(), getUserName(), getUuid(), getCreatedDate());
    }
}
