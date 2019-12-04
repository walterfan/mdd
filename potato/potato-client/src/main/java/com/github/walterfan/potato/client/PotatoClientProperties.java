package com.github.walterfan.potato.client;

import lombok.Data;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.validation.Valid;

/**
 * @Author: Walter Fan
 *
 * potato.registry.url=http://localhost:8761
 *
 * potato.identity.url=http://localhost:9001/identity/api/v1
 *
 * potato.scheduler.url=http://localhost:9002/scheduler/api/v1
 *
 * potato.server.url=http://localhost:9003/potato/api/v1
 *
 * potato.tomato.url=http://localhost:9004/tomato/api/v1
 *
 * potato.web.url=http://localhost:9005/web/api/v1
 *
 **/
@Component
@Data
public class PotatoClientProperties {
    private static final String POTATO_SERVER_URL = "potato_server_url";
    private static final String POTATO_SCHEDULER_URL = "potato_scheduler_url";

    @Autowired
    private Environment env;

    private String potatoScheduleryUrl = "http://scheduler:9002/scheduler/api/v1";

    private String potatoServerUrl = "http://potato:9003/potato/api/v1";

    private String potatoIdentityUrl = "http://identity:9001/identity/api/v1";

    private static final String POTATO_IDENTITY_URL = "potato_identity_url";

    public String getPotatoServerUrl() {
        return env.getProperty(POTATO_SERVER_URL, String.class, potatoServerUrl);
    }

    public String getPotatoSchedulerUrl() {
        return env.getProperty(POTATO_SCHEDULER_URL, String.class, potatoScheduleryUrl);
    }

    public String getPotatoIdentityUrl() {
        return env.getProperty(POTATO_IDENTITY_URL, String.class, potatoIdentityUrl);
    }
    
}
