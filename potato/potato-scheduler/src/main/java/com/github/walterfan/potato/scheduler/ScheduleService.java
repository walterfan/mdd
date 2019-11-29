package com.github.walterfan.potato.scheduler;

import com.github.walterfan.potato.common.dto.RescheduleRequest;
import com.github.walterfan.potato.common.dto.RemindEmailRequest;
import com.github.walterfan.potato.common.dto.RemindEmailResponse;

/**
 * @Author: Walter Fan
 * @Date: 22/6/2019, Sat
 **/
public interface ScheduleService {

    RemindEmailResponse scheduleEmail(RemindEmailRequest scheduleEmailRequest);

    boolean unscheduleEmail(String jobId);

    RemindEmailResponse rescheduleEmail(RescheduleRequest rescheduleRequest);


}
