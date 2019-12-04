package com.github.walterfan.potato.server;

import com.github.walterfan.potato.common.dto.PotatoDTO;
import com.github.walterfan.potato.common.dto.ServiceHealth;
import com.github.walterfan.potato.common.metrics.ApiCallMetricAnnotation;
import com.github.walterfan.potato.common.metrics.LogDetail;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * @Author: Walter Fan
 **/
@RestController
@RequestMapping("/potato/api/v1")
@Slf4j
public class PotatoController {

    @Value("${potato.guest.userId}")
    private String guestUserId;

    @Autowired
    private PotatoService potatoService;


    @Autowired
    private ServiceHealthManager serviceHealthManager;

    @RequestMapping(value = "/potatoes", method = RequestMethod.POST)
    @ApiCallMetricAnnotation(name = "CreatePotato")
    public PotatoDTO create(@RequestBody PotatoDTO potatoRequest) {
        log.info("create potato: {}", potatoRequest);
        if(null == potatoRequest.getUserId()) {
            potatoRequest.setUserId(UUID.fromString(this.guestUserId));
        }
        return potatoService.create(potatoRequest);

    }

    @LogDetail
    @RequestMapping(value = "/potatoes/{id}", method = RequestMethod.GET)
    @ApiCallMetricAnnotation(name = "RetrievePotato")
    public PotatoDTO retrieve(@PathVariable UUID id) {
        return potatoService.retrieve(id);
    }

    @LogDetail
    @RequestMapping(value = "/potatoes/{id}", method = RequestMethod.PUT)
    @ApiCallMetricAnnotation(name = "UpdatePotato")
    public PotatoDTO update(@RequestBody PotatoDTO potatoDto) {
        log.info("update potato: {}", potatoDto);
        return potatoService.update(potatoDto);
    }

    @LogDetail
    @RequestMapping(value = "/potatoes/{id}/start", method = RequestMethod.POST)
    @ApiCallMetricAnnotation(name = "StartPotato")
    public void start(@PathVariable UUID id) {
        log.info("start potato: {}", id);
        potatoService.startPotato(id);
    }

    @LogDetail
    @RequestMapping(value = "/potatoes/{id}/stop", method = RequestMethod.POST)
    @ApiCallMetricAnnotation(name = "StopPotato")
    public void stop(@PathVariable UUID id) {
        log.info("stop potato: {}", id);
        potatoService.stopPotato(id);
    }

    @LogDetail
    @RequestMapping(value = "/potatoes/{id}", method = RequestMethod.DELETE)
    @ApiCallMetricAnnotation(name = "DeletePotato")
    public void delete(@PathVariable UUID id) {
        log.info("delete potato: {}", id);
        potatoService.delete(id);
    }

    @LogDetail
    @RequestMapping(value = "/potatoes", method = RequestMethod.GET)
    @ApiCallMetricAnnotation(name = "ListPotatoes")
    public List<PotatoDTO> list(@RequestParam(value = "page", required = false, defaultValue = "0") Integer page,
                                @RequestParam(value = "size", required = false, defaultValue = "20") Integer size,
                                @RequestParam(value = "userId", required = false) String strUserId) {

        if(StringUtils.isBlank(strUserId)) {
            strUserId = guestUserId;
        }
        UUID userId = UUID.fromString(strUserId);
        log.info("list potato: userId={}", userId);
        return potatoService.list(userId, page, size);
    }

    @LogDetail
    @RequestMapping(value = "/potatoes/search", method = RequestMethod.GET)
    @ApiCallMetricAnnotation(name = "SearchPotato")
    public List<PotatoDTO> search(@RequestParam(value = "keyword", required = false, defaultValue = "") String keyword,
                                  @RequestParam(value = "page", required = false, defaultValue = "0") Integer page,
                                  @RequestParam(value = "size", required = false, defaultValue = "20") Integer size,
                                  @RequestParam(value = "userId", required = false) String strUserId
    ) {

        UUID userId = UUID.fromString(strUserId);
        log.info("search potato, userId={}", userId);
        if (StringUtils.isNotBlank(keyword)) {
            return potatoService.search(userId, keyword, page, size);
        } else {
            return potatoService.list(userId, page, size);
        }

    }

    @RequestMapping(value = "/ping", method = RequestMethod.GET)
    @ApiCallMetricAnnotation(name = "ping")
    public ServiceHealth ping() {
        return serviceHealthManager.checkHealth();
    }

}
