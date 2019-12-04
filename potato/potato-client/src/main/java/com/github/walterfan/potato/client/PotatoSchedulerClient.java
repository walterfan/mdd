package com.github.walterfan.potato.client;

import com.github.walterfan.potato.common.dto.RemindEmailRequest;
import com.github.walterfan.potato.common.dto.RemindEmailResponse;
import com.github.walterfan.potato.common.dto.ServiceHealth;
import com.github.walterfan.potato.common.metrics.ClientCallMetricAnnotation;
import com.github.walterfan.potato.common.metrics.ServiceHealthChecker;
import com.github.walterfan.potato.common.util.JsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * @Author: Walter Fan
 **/
@Component
@Slf4j
public class PotatoSchedulerClient extends AbstractDiscoveryClient {
    public static final String POTATO_SCHEDULER = "potato_scheduler";


    @ClientCallMetricAnnotation(name = "ScheduleRemindEmail", component = "SchedulerService")
    public ResponseEntity<RemindEmailResponse> scheduleRemindEmail (RemindEmailRequest remindEmailRequest) {
        String url = getServiceUrl() + "/reminders";
        log.info("scheduleRemindEmail to {} as {}", url, JsonUtil.toJson(remindEmailRequest));
        return restTemplate.postForEntity(url, remindEmailRequest, RemindEmailResponse.class);
    }

    @Override
    public String getServiceName() {
        return POTATO_SCHEDULER;
    }
}
