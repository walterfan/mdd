package com.github.walterfan.potato.web;

import com.github.walterfan.potato.client.PotatoClient;
import com.github.walterfan.potato.common.dto.PotatoDTO;
import com.github.walterfan.potato.common.dto.ServiceHealth;
import com.github.walterfan.potato.common.metrics.ApiCallMetricAnnotation;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;


import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @Author: Walter Fan
 **/
@Slf4j
@Service
public class PotatoWebService {
    @Value("${spring.application.name}")
    private String serviceName;

    @Value("${server.port}")
    private Integer serverPort;

    @Value("${potato.guest.userId}")
    private String guestUserId;

    @Autowired
    private ServiceHealthManager serviceHealthManager;

    @Autowired
    private PotatoClient potatoClient;
    public PotatoDTO create(@RequestBody PotatoDTO potatoRequest) {

        log.info("create {}", potatoRequest);

        return potatoClient.createPotato(potatoRequest);

    }

    public PotatoDTO retrieve(UUID id) {
        return potatoClient.retrievePotato(id);
    }


    public void update(PotatoDTO potatoDto) {
        potatoClient.updatePotato(potatoDto);
    }


    public void delete(UUID id) {
        potatoClient.deletePotato(id);
    }

    @HystrixCommand(fallbackMethod = "defaultList",
            groupKey = "potato", commandKey = "listPotatoes", threadPoolKey = "potato",
            commandProperties = {
            @HystrixProperty(name = "execution.isolation.thread.timeoutInMilliseconds", value = "30000"),
            @HystrixProperty(name = "circuitBreaker.requestVolumeThreshold", value = "4"),
            @HystrixProperty(name = "circuitBreaker.sleepWindowInMilliseconds", value = "60000"),
            @HystrixProperty(name = "metrics.rollingStats.timeInMilliseconds", value = "180000") },
            threadPoolProperties = {
            @HystrixProperty(name = "coreSize", value = "4"),
            @HystrixProperty(name = "metrics.rollingStats.timeInMilliseconds", value = "180000") })
    public List<PotatoDTO> list(UUID userId, Integer page, Integer size) {
        if(null == userId) {
            userId = UUID.fromString(guestUserId);
        }
        return potatoClient.listPotatoes(userId, page, size);
    }

    public List<PotatoDTO> defaultList(UUID userId, Integer page, Integer size) {
        List<PotatoDTO> retList = new ArrayList<>();
        PotatoDTO potatoDTO = new PotatoDTO();
        potatoDTO.setName("To check potato server status");
        retList.add(potatoDTO);
        return retList;
    }

    public List<PotatoDTO> search(UUID userId,String keyword, Integer page, Integer size) {
        if(null == userId) {
            userId = UUID.fromString(guestUserId);
        }
        return potatoClient.searchPotatoes(userId, keyword, page, size);

    }

    @RequestMapping(value = "/ping", method = RequestMethod.GET)
    @ApiCallMetricAnnotation(name = "ping")
    public ServiceHealth ping() {
        return this.serviceHealthManager.checkHealth();

    }
}
