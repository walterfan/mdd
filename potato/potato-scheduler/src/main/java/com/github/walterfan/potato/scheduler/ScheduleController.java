package com.github.walterfan.potato.scheduler;

import com.github.walterfan.potato.common.dto.RescheduleRequest;
import com.github.walterfan.potato.common.dto.RemindEmailRequest;
import com.github.walterfan.potato.common.dto.RemindEmailResponse;
import com.github.walterfan.potato.common.dto.ServiceHealth;
import com.github.walterfan.potato.common.dto.ServiceState;
import com.github.walterfan.potato.common.metrics.ApiCallMetricAnnotation;
import lombok.extern.slf4j.Slf4j;
import org.h2.util.NetUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.Optional;

@RestController
@RequestMapping("/scheduler/api/v1")
@Slf4j
public class ScheduleController {

    @Value("${spring.application.name}")
    private String serviceName;

    @Value("${server.port}")
    private Integer serverPort;

    @Autowired
    private ScheduleService scheduleService;

    @Autowired
    private ServiceHealthManager serviceHealthManager;

    @RequestMapping(value = "/ping", method = RequestMethod.GET)
    @ApiCallMetricAnnotation(name = "ping")
    public ServiceHealth ping() {
        return serviceHealthManager.checkHealth();

    }



    @PostMapping("/reminders")
    @ApiCallMetricAnnotation(name = "ScheduleRemindEmail")
    public ResponseEntity<RemindEmailResponse> scheduleEmail(@Valid @RequestBody RemindEmailRequest scheduleEmailRequest) {
        log.info("Receive {}", scheduleEmailRequest);
        return ResponseEntity.of(Optional.ofNullable(scheduleService.scheduleEmail(scheduleEmailRequest)));
    }

    @ApiCallMetricAnnotation(name = "RescheduleRemindEmail")
    @PutMapping("/reminders/{jobId}")
    public ResponseEntity<RemindEmailResponse> rescheduleEmail(@Valid @RequestBody RescheduleRequest rescheduleEmailRequest) {
        log.info("rescheduleEmail {}", rescheduleEmailRequest);
        return ResponseEntity.ok(scheduleService.rescheduleEmail(rescheduleEmailRequest));
    }

    @ApiCallMetricAnnotation(name = "DeleteRemindEmail")
    @DeleteMapping("/reminders/{jobId}")
    public void unscheduleEmail(@PathVariable String jobId) {
        log.info("unscheduleEmail {}", jobId);
        scheduleService.unscheduleEmail(jobId);
    }

}