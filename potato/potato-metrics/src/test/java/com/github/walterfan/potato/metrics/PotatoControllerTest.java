package com.github.walterfan.potato.metrics;

import com.fasterxml.jackson.core.type.TypeReference;
import com.github.walterfan.potato.common.config.MetricsConfig;
import com.github.walterfan.potato.common.dto.PotatoDTO;
import com.github.walterfan.potato.common.util.JsonUtil;

import com.github.walterfan.potato.metrics.config.WebConfig;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

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
@ContextConfiguration(classes = { WebConfig.class, MetricsConfig.class})
@Slf4j
public class PotatoControllerTest {

	@Autowired
	private MockMvc mvc;

	@Test
	public void testPing() throws Exception {
		MvcResult result = this.mvc.perform(get("/potato/api/v1/ping")).andExpect(status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$.serviceName").value("potato-service"))
				.andReturn();

		String content = result.getResponse().getContentAsString();

		log.info("-- ping result is {}", content);
	}

	//@Test
	public void testCRUDL() throws Exception {
		UUID userId = UUID.randomUUID();
		String todoItem = "read POSA2";
		String description = "Pattern-Oriented Software Architecture Volume 2: Patterns for Concurrent and Networked Objects";
		PotatoDTO potatoDTO = PotatoDTO.builder()
				.name(todoItem)
				.userId(userId)
				.tags("read,book")
                .priority(1)
				.duration(10L)
				.timeUnit(TimeUnit.DAYS)
				.description(description)
				.scheduleTime(ZonedDateTime.now())
				.deadline(ZonedDateTime.parse("2019-10-10T10:10:10Z"))
				.build();
		log.info("potatoDTO: {}", potatoDTO.toJson());
		MvcResult postResult = this.mvc.perform(post("/potato/api/v1/potatoes")
				.content(potatoDTO.toJson())
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				//andExpect(MockMvcResultMatchers.jsonPath("$.serviceName").value("potato-service"))
				//.andExpect(MockMvcResultMatchers.jsonPath("$.employeeId").exists());
				.andReturn();
		String postContent = postResult.getResponse().getContentAsString();
        PotatoDTO createdPotato = JsonUtil.fromJson(postContent, PotatoDTO.class);
		log.info("-- postPotatoe result is {}", createdPotato.toJson());

		MvcResult listResult = this.mvc.perform(get("/potato/api/v1/potatoes?userId=" + userId.toString()))
				.andExpect(status().isOk())
				//andExpect(MockMvcResultMatchers.jsonPath("$.serviceName").value("potato-service"))
				.andReturn();

		String listContent = listResult.getResponse().getContentAsString();
        log.info("-- list Potato result are {}", listContent);
		List<PotatoDTO> potatoList = JsonUtil.getObjectMapper().readValue(listContent, new TypeReference<List<PotatoDTO>>(){});

        assertEquals(potatoList.size(), 1);
        assertTrue(potatoList.get(0).getName().equals(todoItem));
        assertTrue(potatoList.get(0).getDescription().equals(description));

        createdPotato.setPriority(3);
        MvcResult updateResult = this.mvc.perform(put("/potato/api/v1/potatoes/" + createdPotato.getId().toString())
                .content(createdPotato.toJson())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String updateContent = updateResult.getResponse().getContentAsString();
        PotatoDTO updatedPotato = JsonUtil.fromJson(updateContent, PotatoDTO.class);
        log.info("-- updated Potato is {}", updatedPotato);
        assertTrue(updatedPotato.getPriority() == 3);

        this.mvc.perform(delete("/potato/api/v1/potatoes/" + updatedPotato.getId().toString()))
                .andExpect(status().isOk());
        this.mvc.perform(get("/potato/api/v1/potatoes/" + updatedPotato.getId().toString()))
                .andExpect(status().is4xxClientError());

	}


}