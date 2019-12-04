package com.github.walterfan.potato.scheduler;

import com.github.walterfan.potato.common.dto.RescheduleRequest;
import com.github.walterfan.potato.common.dto.RemindEmailRequest;
import com.github.walterfan.potato.common.dto.RemindEmailResponse;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.UUID;

/**
 * @Author: Walter Fan
 **/
@Service
@Slf4j
public class ScheduleServiceImpl implements ScheduleService {
    public static final String EMAIL_JOB_GROUP = "emailReminder";
    @Autowired
    private Scheduler scheduler;

    @Override
    public RemindEmailResponse scheduleEmail(RemindEmailRequest scheduleEmailRequest) {
        log.info("schedule {}", scheduleEmailRequest);
        try {
            ZonedDateTime dateTime = scheduleEmailRequest.getDateTime();

            JobDetail jobDetail = buildJobDetail(scheduleEmailRequest);
            Trigger trigger = buildJobTrigger(jobDetail.getKey(), dateTime);

            Date scheduledDate = scheduler.scheduleJob(jobDetail, trigger);

            RemindEmailResponse scheduleEmailResponse = new RemindEmailResponse(true,
                    jobDetail.getKey().getName(), jobDetail.getKey().getGroup(), "Email Scheduled Successfully at " + scheduledDate);
            log.info("Send {}", scheduleEmailResponse);
            return scheduleEmailResponse;
        } catch (SchedulerException ex) {
            log.error("Error scheduling email", ex);

            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR, "dateTime must be after current time");
        }
    }


    @Override
    public boolean unscheduleEmail(String jobId) {

        TriggerKey triggerKey = new TriggerKey(jobId, EMAIL_JOB_GROUP);

        try {
             return scheduler.unscheduleJob(triggerKey);
        } catch (SchedulerException ex) {
            log.error("Unschedule email task error: ", ex);

            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR, "dateTime must be after current time");
        }
    }

    @Override
    public RemindEmailResponse rescheduleEmail(RescheduleRequest rescheduleRequest) {
        ZonedDateTime dateTime = rescheduleRequest.getDateTime();

        JobKey jobKey = new JobKey(rescheduleRequest.getJobId(), EMAIL_JOB_GROUP);
        TriggerKey triggerKey = new TriggerKey(jobKey.getName(), jobKey.getGroup());

        try {
            Trigger trigger = this.buildJobTrigger(jobKey, dateTime);
            JobDetail jobDetail = buildJobDetail(rescheduleRequest);

            Date scheduledDate = scheduler.rescheduleJob(triggerKey, trigger);

            RemindEmailResponse scheduleEmailResponse = new RemindEmailResponse(true,
                    jobDetail.getKey().getName(), jobDetail.getKey().getGroup(), "Email Scheduled Successfully at " + scheduledDate);
            log.info("Send {}", scheduleEmailResponse);
            return scheduleEmailResponse;
        } catch (SchedulerException ex) {
            log.error("Error scheduling email", ex);

            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR, "dateTime must be after current time");
        }

    }

    private ZonedDateTime getZonedDateTime(Instant theTime, ZoneId timeZone) {

        ZonedDateTime dateTime = ZonedDateTime.ofInstant(theTime, timeZone);
        if (dateTime.isBefore(ZonedDateTime.now())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "dateTime must be after current time");
        }
        return dateTime;
    }


    private JobDetail buildJobDetail(RemindEmailRequest scheduleEmailRequest) {
        JobDataMap jobDataMap = new JobDataMap();

        jobDataMap.put("email", scheduleEmailRequest.getEmail());
        jobDataMap.put("subject", scheduleEmailRequest.getSubject());
        jobDataMap.put("body", scheduleEmailRequest.getBody());

        return JobBuilder.newJob(EmailJob.class)
                .withIdentity(UUID.randomUUID().toString(), EMAIL_JOB_GROUP)
                .withDescription("Send Email Job")
                .usingJobData(jobDataMap)
                .storeDurably()
                .build();
    }

    private Trigger buildJobTrigger(JobKey jobKey, ZonedDateTime startAt) {
        return TriggerBuilder.newTrigger()
                .withIdentity(jobKey.getName(), jobKey.getGroup())
                .withDescription("Send Email Trigger")
                .startAt(Date.from(startAt.toInstant()))
                .withSchedule(SimpleScheduleBuilder.simpleSchedule().withMisfireHandlingInstructionFireNow())
                .build();
    }
}
