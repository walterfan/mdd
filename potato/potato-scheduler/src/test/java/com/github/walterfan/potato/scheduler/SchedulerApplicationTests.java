package com.github.walterfan.potato.scheduler;

import com.github.walterfan.potato.common.dto.*;
import com.github.walterfan.potato.common.util.JsonUtil;
import com.squareup.moshi.Json;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@Slf4j
public class SchedulerApplicationTests {

    @Autowired
    private MockMvc mvc;

    @Test
    public void testPing() throws Exception {
        MvcResult result = this.mvc.perform(get("/scheduler/api/v1/ping")).andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.serviceName").value("potato-scheduler"))
                .andReturn();

        String content = result.getResponse().getContentAsString();
        log.info("content: {}", content);
        ServiceHealth serviceHealth = JsonUtil.fromJson(content, ServiceHealth.class);
        assertEquals(serviceHealth.getServiceName(),"potato-scheduler" );
        assertEquals(serviceHealth.getServiceState(), ServiceState.UP );
        log.info(content);
    }


    @Test
    public void testEmailTask() throws Exception {

        RemindEmailRequest remindEmailRequest = new RemindEmailRequest();
        remindEmailRequest.setEmail("walterfan@qq.com");
        remindEmailRequest.setBody("Hi Walter\n\n, it's a testing message. \n\n Reqards, Walter");
        remindEmailRequest.setSubject("Walter Test Email");
        remindEmailRequest.setDateTime(ZonedDateTime.now().plusSeconds(7200));

        log.info("request: {}", remindEmailRequest);

        MvcResult result = this.mvc.perform(post("/scheduler/api/v1/reminders")
                .content(remindEmailRequest.toJson())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        log.info(content);
        RemindEmailResponse remindResponse = JsonUtil.fromJson(content, RemindEmailResponse.class);
        log.info(remindResponse.toJson());

        RescheduleRequest rescheduleRequest = new RescheduleRequest();
        BeanUtils.copyProperties(remindEmailRequest, rescheduleRequest);
        rescheduleRequest.setJobId(remindResponse.getJobId());

        result = this.mvc.perform(put("/scheduler/api/v1/reminders/"  + remindResponse.getJobId())
                .content(rescheduleRequest.toJson())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String content2 = result.getResponse().getContentAsString();
        RemindEmailResponse remindResponse2 = JsonUtil.fromJson(content2, RemindEmailResponse.class);
        log.info(remindResponse2.toJson());

        this.mvc.perform(delete("/scheduler/api/v1/reminders/" + remindResponse.getJobId()))
                .andExpect(status().isOk());
    }
}

