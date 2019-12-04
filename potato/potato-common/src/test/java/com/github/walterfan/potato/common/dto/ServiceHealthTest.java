package com.github.walterfan.potato.common.dto;

import com.github.walterfan.potato.common.util.JsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

/**
 * @Author: Walter Fan
 **/
@Slf4j
public class ServiceHealthTest {

    @Test
    public void testJson() {
        ServiceHealth serviceHealth = new ServiceHealth("potato-identity","unknown",ServiceState.UP);


        String strJson = serviceHealth.toJson();
        log.info(strJson);
        ServiceHealth serviceHealth2 = JsonUtil.fromJson(strJson, ServiceHealth.class);
        log.info(serviceHealth2.toJson());

    }
}
