package com.github.walterfan.potato.common.domain;

import com.github.walterfan.potato.common.util.JsonUtil;
import com.squareup.moshi.Json;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @Author: Walter Fan
 * @Date: 9/6/2019, Sun
 **/
@Slf4j
public class UserTest {

    @Test
    public void testJson() {
        User user = User.builder().name("walter").email("walter@china.com").build();
        String strJson = JsonUtil.toJson(user);
        log.info(strJson);

        User user2 = JsonUtil.fromJson(strJson, User.class);
        assertEquals(user.getName(), user2.getName());
    }
}
