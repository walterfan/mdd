package com.github.walterfan.potato.web;

import com.codahale.metrics.InstrumentedExecutorService;
import com.codahale.metrics.MetricRegistry;
import com.github.walterfan.potato.client.PotatoClient;
import com.github.walterfan.potato.common.metrics.AbstractServiceHealthChecker;
import com.github.walterfan.potato.common.metrics.ServiceHealthChecker;
import com.github.walterfan.potato.common.metrics.ServiceHealthIndicator;
import com.github.walterfan.potato.common.util.NetworkUtil;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
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
    private MetricRegistry metricRegistry;

    @Autowired
    private InstrumentedExecutorService executorService;

    @Autowired
    private PotatoClient potatoClient;

    private Map<String, Boolean> UPSTREAM_SERVICES = ImmutableMap.of(
            "diskSpace", true,
            "potatoService",true);

    @PostConstruct
    public void initialize() {
        ServiceHealthIndicator serviceHealthIndicator
                = new ServiceHealthIndicator(potatoClient, metricRegistry, executorService,true);

        this.healthIndicatorRegistry.register("potatoService", serviceHealthIndicator);
        //this.healthIndicatorRegistry.register("influxDB", new InfluxDbHealthIndicator(InfluxDbHealthIndicator));
    }
    @Override
    public  Map<String, Boolean> getUpstreamServices() {
        return this.UPSTREAM_SERVICES;
    }

    @Override
    public String getServiceName() {
        return this.serviceName;
    }

    @Override
    public String getServiceUrl() {
        return String.format("http://%s:%d/%s/api/v1/ping" , NetworkUtil.getLocalAddress() , this.serverPort, this.serviceName);
    }
}
