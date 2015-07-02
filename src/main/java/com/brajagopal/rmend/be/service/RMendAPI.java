package com.brajagopal.rmend.be.service;
import com.brajagopal.rmend.be.service.resources.ContentRecommenderResource;

import javax.ws.rs.core.Application;
import java.util.HashSet;
import java.util.Set;

/**
 * @author <bxr4261>
 */
public class RMendAPI extends Application {

    @Override
    public Set<Class<?>> getClasses() {
        final Set<Class<?>> classes = new HashSet<Class<?>>();
        classes.add(ContentRecommenderResource.class);
        return classes;
    }
}
