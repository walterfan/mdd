package com.github.walterfan.potato.server;

import com.codahale.metrics.InstrumentedExecutorService;
import com.codahale.metrics.MetricRegistry;
import com.github.walterfan.potato.client.PotatoSchedulerClient;

import com.github.walterfan.potato.common.metrics.AbstractServiceHealthChecker;
import com.github.walterfan.potato.common.metrics.ServiceHealthChecker;
import com.github.walterfan.potato.common.metrics.ServiceHealthIndicator;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import lombok.extern.slf4j.Slf4j;
import org.h2.util.NetUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

import java.util.Date;

import java.util.Map;
import java.util.Set;

/**
 * @Author: Walter Fan
 **/
@Slf4j
@Component
public class ServiceHealthManager extends AbstractServiceHealthChecker implements ServiceHealthChecker {
    @Value("${spring.application.name}")
    private String serviceName;

    @Value("${server.port}")
    private Integer serverPort;

    @Autowired
    private PotatoSchedulerClient potatoSchedulerClient;

    @Autowired
    private MetricRegistry metricRegistry;

    @Autowired
    private InstrumentedExecutorService executorService;

    private Map<String, Boolean> UPSTREAM_SERVICES = ImmutableMap.of(
            "diskSpace", true,
            "db", true,
            "schedulerService", true);

    @PostConstruct
    public void initialize() {
        ServiceHealthIndicator serviceHealthIndicator
                = new ServiceHealthIndicator(potatoSchedulerClient, metricRegistry, executorService, true);
        this.healthIndicatorRegistry.register("schedulerService", serviceHealthIndicator);
    }

    @Override
    public Map<String, Boolean> getUpstreamServices() {
        return this.UPSTREAM_SERVICES;
    }

    @Override
    public String getServiceName() {
        return this.serviceName;
    }

    @Override
    public String getServiceUrl() {
        return String.format("http://%s:%d/%s/api/v1/ping" , NetUtils.getLocalAddress() , this.serverPort, this.serviceName);
    }
}
