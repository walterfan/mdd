package com.github.walterfan.potato.registry;

import com.github.walterfan.potato.common.domain.User;
import com.github.walterfan.potato.common.dto.ServiceHealth;
import com.github.walterfan.potato.common.dto.ServiceState;
import com.github.walterfan.potato.common.metrics.ApiCallMetricAnnotation;
import lombok.extern.slf4j.Slf4j;
import org.h2.util.NetUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author: Walter Fan
 **/
@RestController
@RequestMapping("/registry/api/v1")
@Slf4j
public class PotatoRegistryController {

    @Value("${spring.application.name}")
    private String serviceName;

    @Value("${server.port}")
    private Integer serverPort;

    @RequestMapping(value = "/users", method = RequestMethod.POST)
    @ApiCallMetricAnnotation(name = "CreateUser")
    public User create(@RequestBody User potatoRequest) {
        log.info("create {}", potatoRequest);
        return User.builder().email("walter.fan@gmail.com").build();

    }

    @RequestMapping(value = "/ping", method = RequestMethod.GET)
    @ApiCallMetricAnnotation(name = "ping")
    public ServiceHealth ping() {
        return new ServiceHealth(this.serviceName, NetUtils.getLocalAddress()+ ":"+ serverPort, ServiceState.UP);

    }
}
