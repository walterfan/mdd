package com.github.walterfan.potato.common.metrics;

import com.github.walterfan.potato.common.dto.ServiceHealth;

import java.util.Set;

public interface ServiceHealthChecker {

    ServiceHealth checkHealth();

    String getServiceName();

    String getServiceUrl();

    default Boolean isRequiredUpstreamService(String name) {
        return true;
    }
}
