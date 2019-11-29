package com.github.walterfan.potato.identity;

import com.github.walterfan.potato.common.dto.ServiceHealth;
import com.github.walterfan.potato.common.util.JsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@Slf4j
public class PotatoIdentityApplicationTest {

	@Autowired
	private MockMvc mvc;

	@Test
	public void testPing() throws Exception {
        MvcResult result = this.mvc.perform(get("/identity/api/v1/ping")).andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.serviceName").value("potato-identity"))
                .andReturn();

        String content = result.getResponse().getContentAsString();

        log.info(content);
	}
}
