package com.github.walterfan.potato.common.metrics;

import com.github.walterfan.potato.common.dto.ServiceHealth;
import com.github.walterfan.potato.common.dto.ServiceState;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.health.HealthIndicatorRegistry;
import org.springframework.boot.actuate.health.Status;

import java.util.*;

/**
 * @Author: Walter Fan
 **/
@Slf4j
public abstract class AbstractServiceHealthChecker implements ServiceHealthChecker {

    @Autowired
    protected HealthIndicatorRegistry healthIndicatorRegistry;

    public boolean checkDependencies(List<ServiceHealth> serviceHealths) {

        return  healthIndicatorRegistry
                .getAll()
                .entrySet()
                .stream()
                .filter( entry -> null != isRequiredUpstreamService(entry.getKey()))
                .allMatch((entry) -> {
                    HealthIndicator healthIndicator = entry.getValue();
                    log.info("{}, {}", entry.getKey(), healthIndicator.health());
                    serviceHealths.add(getServiceHealth(entry.getKey(), healthIndicator.health()));
                    return healthIndicator.health().getStatus().equals(Status.UP);
                });
    }

    private ServiceHealth getServiceHealth(String name, Health health) {
        ServiceState serviceState = ServiceState.fromJson(health.getStatus().getCode());
        ServiceHealth serviceHealth =  new ServiceHealth(name, "", serviceState);
        serviceHealth.setMessage(health.toString());
        serviceHealth.setRequired(isRequiredUpstreamService(name));
        return serviceHealth;
    }
    @Override
    public ServiceHealth checkHealth() {
        List<ServiceHealth> serviceHealthList = new ArrayList<>();

        ServiceState serviceState = checkDependencies(serviceHealthList)? ServiceState.UP: ServiceState.DOWN;
        ServiceHealth serviceHealth = new ServiceHealth(this.getServiceName(), getServiceUrl(), serviceState);
        serviceHealth.setLastUpdatedTime(new Date());
        serviceHealth.setUpstreamServices(serviceHealthList);
        serviceHealth.setMessage(serviceState.toString());
        return serviceHealth;
    }

    public abstract Map<String, Boolean> getUpstreamServices();

    @Override
    public Boolean isRequiredUpstreamService(String name) {
        return this.getUpstreamServices().get(name);
    }

}
