package com.brajagopal.rmend.be.utils.bots;

import com.brajagopal.rmend.be.beans.AutomationBotTraceBean;
import com.google.api.client.http.HttpResponse;

import java.util.Collection;

/**
 * @author <bxr4261>
 */
public class IAutomationBaseBot {

    private Collection<AutomationBotTraceBean> successResponses;
    private Collection<AutomationBotTraceBean> failedResponses;

    public static void track(HttpResponse response) {
        
    }
}
