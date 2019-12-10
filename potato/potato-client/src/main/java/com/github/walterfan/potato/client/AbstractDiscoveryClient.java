package com.github.walterfan.potato.client;

import com.github.walterfan.potato.common.dto.ServiceHealth;
import com.github.walterfan.potato.common.metrics.ServiceHealthChecker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

@Slf4j
public abstract class AbstractDiscoveryClient  implements ServiceHealthChecker {
    @Autowired
    protected RestTemplate restTemplate;

    @Autowired
    protected PotatoClientProperties potatoClientProperties;

    @Autowired
    protected DiscoveryClient discoveryClient;

    @Override
    public ServiceHealth checkHealth() {
        String url = getServiceUrl() + "/ping";
        log.info("checkHealth to {} ", url);
        return restTemplate.getForObject(url, ServiceHealth.class);
    }

    @Override
    public String getServiceUrl() {
        String serviceUrl = null;
        String serviceName = this.getServiceName();
        if(null != discoveryClient) {
            serviceUrl = findServiceUrl(serviceName);
        }
        if(null == serviceUrl) {
            if("potato_service".equalsIgnoreCase(serviceName)) {
                serviceUrl = potatoClientProperties.getPotatoServerUrl();
            } else if("potato_scheduler".equalsIgnoreCase(serviceName)) {
                serviceUrl = potatoClientProperties.getPotatoSchedulerUrl();
            } else {
                log.error("Cannot get the service url for {}", serviceName);
            }
            log.info("configure: {}'s url is {}", serviceName, serviceUrl);

        }

        return serviceUrl;
    }

    private String findServiceUrl(String serviceName) {
        try {
            String serviceUrl = discoveryClient.getInstances(serviceName)
                    .stream()
                    .map(si -> si.getUri())
                    .findFirst()
                    .map(x -> x.toString())
                    .orElse(null);
            log.info("discovery: {}'s url is {}", serviceName, serviceUrl);
            return serviceUrl;
        } catch (Exception e) {
            log.error("findServceUrl error", e);
            return null;
        }
    }

    @Override
    public abstract String getServiceName();
}
