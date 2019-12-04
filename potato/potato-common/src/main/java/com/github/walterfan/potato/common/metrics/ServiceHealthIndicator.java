package com.github.walterfan.potato.common.metrics;

import com.github.walterfan.potato.common.dto.ServiceHealth;
import lombok.Getter;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.health.Status;

/**
 * @Author: Walter Fan
 **/
public class ServiceHealthIndicator implements HealthIndicator {

    public static final String SERVICE_NAME = "serviceName";
    public static final String SERVICE_URL = "serviceUrl";
    public static final String SERVICE_STATE = "serviceState";
    public static final String REQUIRED = "required";
    public static final String MESSAGE = "message";

    @Getter
    private final ServiceHealthChecker healthChecker;

    @Getter
    private final Boolean  required;

    public ServiceHealthIndicator(ServiceHealthChecker healthChecker, Boolean required) {
        this.healthChecker = healthChecker;
        this.required = required;
    }

    @Override
    public Health health() {
        Health health = null;
        try {
            ServiceHealth serviceHealth = healthChecker.checkHealth();
            health = Health.status(serviceHealth.getServiceState().name())
                    .withDetail(SERVICE_NAME, serviceHealth.getServiceName())
                    .withDetail(SERVICE_URL, serviceHealth.getServiceUrl())
                    .withDetail(SERVICE_STATE, serviceHealth.getServiceState())
                    .withDetail(REQUIRED, serviceHealth.getRequired())
                    .withDetail(MESSAGE, serviceHealth.getMessage())
                    .build();
        } catch (Exception e) {
            health = Health.down()
                    .withDetail(SERVICE_NAME, healthChecker.getServiceName())
                    .withDetail(SERVICE_URL, healthChecker.getServiceUrl())
                    .withDetail(SERVICE_STATE, Status.DOWN.getCode())
                    .withDetail(REQUIRED, this.required)
                    .withException(e)
                    .build();
        }


        return health;
    }
}
