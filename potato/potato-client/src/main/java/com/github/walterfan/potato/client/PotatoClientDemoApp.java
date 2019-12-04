package com.github.walterfan.potato.client;

import com.github.walterfan.potato.common.dto.PotatoDTO;
import com.github.walterfan.potato.common.dto.RemindEmailRequest;
import com.github.walterfan.potato.common.dto.RemindEmailResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @Author: Walter Fan
 **/
@SpringBootApplication
@Slf4j
public class PotatoClientDemoApp implements CommandLineRunner {

    public static void main(String args[]) {
        SpringApplication.run(PotatoClientDemoApp.class);
    }

    @Autowired
    private IdentityClient identityClient;

    @Autowired
    private PotatoSchedulerClient schedulerClient;

    @Autowired
    private PotatoClient potatoClient;

    @Override
    public void run(String... args) {
        log.info("--- command line runner ---");

        for (int i = 0; i < args.length; ++i) {
            log.info("args[{}]: {}", i, args[i]);
        }

        createPotato();
    }


    private void createPotato() {
        PotatoDTO potatoDTO = PotatoDTO.builder()
                .name("Browse pos2 book")
                .description("pos2: The Definitive Guide")
                .priority(2)
                .userId(UUID.fromString("53a3093e-6436-4663-9125-ac93d2af91f9"))
                .tags("book")
                .scheduleTime(ZonedDateTime.ofInstant(Instant.ofEpochMilli(System.currentTimeMillis() + 300 * 1000), ZoneId.systemDefault()))
                .deadline(ZonedDateTime.ofInstant(Instant.ofEpochMilli(System.currentTimeMillis() + 600 * 1000), ZoneId.systemDefault()))
                .duration(5L)
                .timeUnit(TimeUnit.MINUTES)
                .build();
        PotatoDTO respEntity = potatoClient.createPotato(potatoDTO);
        log.info("PotatoDTO is {}", respEntity);
    }

    private void createRemindEmail() {
        RemindEmailRequest remindEmailRequest = RemindEmailRequest.builder()
                .email("fanyamin@hotmail.com")
                .subject("write appendix")
                .body("* fabric\n * script\n\n\n regards,\nwalter\n")
                .dateTime(ZonedDateTime.of(2019, 8,30,11,5,0, 0, ZoneOffset.UTC))
                .build();

        ResponseEntity<RemindEmailResponse> ret =  schedulerClient.scheduleRemindEmail(remindEmailRequest);
        log.info("status code is {}", ret.getStatusCode());
    }


}
