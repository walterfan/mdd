package com.github.walterfan.potato.common.metrics;

import com.codahale.metrics.InstrumentedExecutorService;
import com.codahale.metrics.MetricRegistry;
import com.github.walterfan.potato.common.dto.ServiceHealth;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.health.Status;

import java.util.concurrent.*;

/**
 * @Author: Walter Fan
 **/
public class ServiceHealthIndicator implements HealthIndicator,Callable<Health> {

    public static final String SERVICE_NAME = "serviceName";
    public static final String SERVICE_URL = "serviceUrl";
    public static final String SERVICE_STATE = "serviceState";
    public static final String REQUIRED = "required";
    public static final String MESSAGE = "message";

    @Getter
    private final ServiceHealthChecker healthChecker;
    @Getter
    private final MetricRegistry metricRegistry;
    @Getter
    private final InstrumentedExecutorService executorService;

    @Getter
    private final Boolean  required;

    public ServiceHealthIndicator(ServiceHealthChecker healthChecker,
                                  MetricRegistry metricRegistry,
                                  InstrumentedExecutorService executorService,
                                  Boolean required) {
        this.healthChecker = healthChecker;
        this.metricRegistry = metricRegistry;
        this.executorService = executorService;
        this.required = required;

    }

    @Override
    public Health health() {
        Future<Health> futureHealth = this.executorService.submit(this);
        try {
            return futureHealth.get(1000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException |ExecutionException |TimeoutException e) {
            return getHealthDown(e);
        }
    }

    @Override
    public Health call() {
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
            health = getHealthDown(e);
        }
        return health;
    }

    private Health getHealthDown(Exception e) {
        Health health;
        health = Health.down()
                .withDetail(SERVICE_NAME, healthChecker.getServiceName())
                .withDetail(SERVICE_URL, healthChecker.getServiceUrl())
                .withDetail(SERVICE_STATE, Status.DOWN.getCode())
                .withDetail(REQUIRED, this.required)
                .withException(e)
                .build();
        return health;
    }
}
