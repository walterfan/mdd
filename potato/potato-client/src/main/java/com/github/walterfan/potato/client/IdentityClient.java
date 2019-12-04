package com.github.walterfan.potato.client;


import com.github.walterfan.potato.common.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

/**
 * @Author: Walter Fan
 **/
@Component
public class IdentityClient {
    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private PotatoClientProperties potatoClientProperties;

    public User getUser(UUID userId) {
        return restTemplate.getForObject(potatoClientProperties.getPotatoIdentityUrl() + "/users/" + userId, User.class);
    }
}
