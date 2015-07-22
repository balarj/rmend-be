package com.brajagopal.rmend.be.beans;

/**
 * @author <bxr4261>
 */
@SuppressWarnings("unused")
public class AutomationBotTraceBean {

    private Long uid;
    private String topic;
    private Long docNumber;
    private Long timestamp;
    private Integer statusCode;

    public AutomationBotTraceBean(Long _uid, Long _docNumber, String _topic, Integer _statusCodde) {
        this.uid = _uid;
        this.docNumber = _docNumber;
        this.topic = _topic;
        this.timestamp = System.currentTimeMillis();
        this.statusCode = _statusCodde;
    }

    public Long getUid() {
        return uid;
    }

    public Long getDocNumber() {
        return docNumber;
    }

    public Integer getStatusCode() {
        return statusCode;
    }

    public String getTopic() {
        return topic;
    }

    @Override
    public String toString() {
        return "AutomationBotTraceBean{" +
                "uid=" + uid +
                ", topic='" + topic + '\'' +
                ", docNumber=" + docNumber +
                ", timestamp=" + timestamp +
                ", statusCode=" + statusCode +
                '}';
    }
}