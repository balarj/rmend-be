package com.brajagopal.rmend.be.entities;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * @author <bxr4261>
 */
@Entity
@Table(name = "IMPRESSIONS_USERS")
public class ImpressionUsersEntity {

    @Id
    private Long userId;

    private ImpressionUsersEntity(Long _userId) {
        userId = _userId;
    }

    public static ImpressionUsersEntity createInstance(Long userId) {
        return new ImpressionUsersEntity(userId);
    }

    public ImpressionUsersEntity() {}

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    @Override
    public String toString() {
        return "ImpressionUsersEntity {" +
                "userId=" + userId +
                '}';
    }
}
