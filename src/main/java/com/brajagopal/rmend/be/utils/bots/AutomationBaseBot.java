package com.brajagopal.rmend.be.utils.bots;

import com.brajagopal.rmend.be.beans.AutomationBotTraceBean;
import com.brajagopal.rmend.be.service.resources.BaseResource;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;

import javax.ws.rs.core.Response;

/**
 * @author <bxr4261>
 */
public class AutomationBaseBot extends BaseResource {

    private Multimap<Response.Status, AutomationBotTraceBean> responseTraceBeans;

    protected final Long uid;
    protected final String topic;

    protected AutomationBaseBot(String _userId, String _topic) {
        responseTraceBeans = HashMultimap.create();

        if (_userId == null) {
            throw new NullPointerException("UID is a required environment variable (cannot be null");
        }

        if (_topic == null) {
            throw new NullPointerException("TOPIC is a required environment variable (cannot be null");
        }

        try {
            this.uid = Long.parseLong(_userId);
        }
        catch (NumberFormatException nfe) {
            throw new NumberFormatException("UID has to be of type Long (not a valid UID");
        }
        this.topic = _topic;
    }

    public void track(HttpResponse response) {
        Response.Status responseStatus =
                Response.Status.fromStatusCode(response.getStatusLine().getStatusCode());

        responseTraceBeans.put(responseStatus,
                new AutomationBotTraceBean(
                        uid,
                        Long.parseLong(response.getFirstHeader("X-Document-Number").getValue()),
                        topic,
                        responseStatus.getStatusCode()
                )
        );

        EntityUtils.consumeQuietly(response.getEntity());
    }
}
