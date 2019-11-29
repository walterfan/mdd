package com.github.walterfan.potato.common.dto;

import com.github.walterfan.potato.common.util.JsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;

/**
 * @Author: Walter Fan
 * @Date: 9/6/2019, Sun
 **/
@Slf4j
public class PotatoDTOTest {

    @Test
    public void testJson() {
        PotatoDTO potatoDTO = new PotatoDTO();
        potatoDTO.setName("read book");
        potatoDTO.setDuration(2L);
        potatoDTO.setTimeUnit(TimeUnit.DAYS);
        potatoDTO.setDeadline(ZonedDateTime.parse("2019-08-13T21:57:48+08:00"));

        String strJson  = potatoDTO.toJson();
        log.info(strJson);
        PotatoDTO potatoDTO2 = JsonUtil.fromJson(strJson, PotatoDTO.class);

        assertEquals(potatoDTO.getName(), potatoDTO2.getName());
        log.info("deserialized deadline: {}", potatoDTO2.getDeadline());
        assertEquals(potatoDTO.getDeadline().toEpochSecond(), potatoDTO2.getDeadline().toEpochSecond());


    }
}
