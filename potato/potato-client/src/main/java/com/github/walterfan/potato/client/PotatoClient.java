package com.github.walterfan.potato.client;

import com.github.walterfan.potato.common.dto.PotatoDTO;
import com.github.walterfan.potato.common.dto.ServiceHealth;
import com.github.walterfan.potato.common.metrics.ClientCallMetricAnnotation;
import com.github.walterfan.potato.common.metrics.ServiceHealthChecker;
import com.github.walterfan.potato.common.util.JsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
@Slf4j
public class PotatoClient extends AbstractDiscoveryClient {

	public static final String POTATO_SERVICE = "potato_service";

	@Override
	public String getServiceName() {
		return POTATO_SERVICE;
	}

	@ClientCallMetricAnnotation(name = "createPotato", component = "PotatoService")
	public PotatoDTO createPotato (PotatoDTO potatoReques) {
		String url = getServiceUrl() + "/potatoes";
		log.info("createPotato to {} as {}", url, JsonUtil.toJson(potatoReques));
		return restTemplate.postForObject(url, potatoReques, PotatoDTO.class);
	}

	@ClientCallMetricAnnotation(name = "retrievePotato", component = "PotatoService")
	public PotatoDTO retrievePotato (UUID potatoId) {
		String url = getServiceUrl() + "/potatoes/" + potatoId.toString();
		log.info("retrievePotato from {}", url);
		return restTemplate.getForObject(url, PotatoDTO.class);
	}

	@ClientCallMetricAnnotation(name = "updatePotato", component = "PotatoService")
	public void updatePotato (PotatoDTO potatoRequest) {
		String url = getServiceUrl() + "/potatoes/" + potatoRequest.getId();
		log.info("updatePotato to {} as {}", url, JsonUtil.toJson(potatoRequest));
		restTemplate.put(url, potatoRequest, PotatoDTO.class);
	}

	@ClientCallMetricAnnotation(name = "deletePotato", component = "PotatoService")
	public void deletePotato (UUID potatoId) {
		String url = getServiceUrl() + "/potatoes/" + potatoId.toString();
		log.info("deletePotato as {}", url);
		restTemplate.delete(url);
	}

	@ClientCallMetricAnnotation(name = "startPotato", component = "PotatoService")
	public void startPotato (UUID potatoId) {
		String url = getServiceUrl() + "/potatoes/" + potatoId.toString();
		log.info("deletePotato as {}", url);
		restTemplate.postForObject(url, null, void.class);
	}

	@ClientCallMetricAnnotation(name = "stopPotato", component = "PotatoService")
	public void stopPotato (UUID potatoId) {
		String url = getServiceUrl() + "/potatoes/" + potatoId.toString() + "/stop";
		log.info("deletePotato as {}", url);
		restTemplate.postForObject(url, null, void.class);
	}

	@ClientCallMetricAnnotation(name = "listPotato", component = "PotatoService")
	public List<PotatoDTO> listPotatoes (UUID userId, Integer page, Integer size) {
		String url = getServiceUrl() + "/potatoes";
		log.info("listPotatoes as {}", url);
		Map<String, Object> params = new HashMap<>();
		params.put("userId", userId);
		params.put("page", page);
		params.put("size", size);

		PotatoDTO[] potatoes = restTemplate.getForObject(url, PotatoDTO[].class, params);
		return Arrays.asList(potatoes);
	}
	@ClientCallMetricAnnotation(name = "searchPotato", component = "PotatoService")
	public List<PotatoDTO> searchPotatoes (UUID userId, String keyword, Integer page, Integer size) {
		String url = getServiceUrl() + "/potatoes";
		log.info("listPotatoes as {}", url);
		Map<String, Object> params = new HashMap<>();
		params.put("userId", userId);
		params.put("keyword", keyword);
		params.put("page", page);
		params.put("size", size);

		PotatoDTO[] potatoes = restTemplate.getForObject(url, PotatoDTO[].class, params);
		return Arrays.asList(potatoes);
	}

}
