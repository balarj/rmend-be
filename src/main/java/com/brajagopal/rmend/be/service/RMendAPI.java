package com.brajagopal.rmend.be.service;
import com.brajagopal.rmend.be.utils.GsonProvider;
import org.glassfish.jersey.server.ResourceConfig;

/**
 * @author <bxr4261>
 */
public class RMendAPI extends ResourceConfig {

    public RMendAPI() {
        //register(JacksonFeature.class);
        register(GsonProvider.class);
    }
}
