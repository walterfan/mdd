package com.github.walterfan.potato.web;



import com.github.walterfan.potato.client.PotatoClient;
import com.github.walterfan.potato.common.dto.PotatoDTO;
import com.github.walterfan.potato.common.dto.ServiceHealth;
import com.github.walterfan.potato.common.dto.ServiceState;
import com.github.walterfan.potato.common.metrics.ApiCallMetricAnnotation;
import com.github.walterfan.potato.common.metrics.LogDetail;
import com.github.walterfan.potato.common.util.NetworkUtil;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * Created by yafan on 22/4/2018.
 */
@RestController
@RequestMapping("/api/v1")
@Slf4j
public class PotatoApiController {

    @Autowired
    private PotatoWebService potatoWebService;


    @RequestMapping(value = "/potatoes", method = RequestMethod.POST)
    @ApiCallMetricAnnotation(name = "CreatePotato")
    public PotatoDTO create(@RequestBody PotatoDTO potatoRequest) {

        log.info("create {}", potatoRequest);

        return potatoWebService.create(potatoRequest);

    }

    @LogDetail
    @RequestMapping(value = "/potatoes/{id}", method = RequestMethod.GET)
    @ApiCallMetricAnnotation(name = "RetrievePotato")
    public PotatoDTO retrieve(@PathVariable UUID id) {
        return potatoWebService.retrieve(id);
    }

    @LogDetail
    @RequestMapping(value = "/potatoes/{id}", method = RequestMethod.PUT)
    @ApiCallMetricAnnotation(name = "UpdatePotato")
    public void update(@RequestBody PotatoDTO potatoDto) {
        potatoWebService.update(potatoDto);
    }

    @LogDetail
    @RequestMapping(value = "/potatoes/{id}", method = RequestMethod.DELETE)
    @ApiCallMetricAnnotation(name = "DeletePotato")
    public void delete(@PathVariable UUID id) {
        potatoWebService.delete(id);
    }

    @LogDetail
    @RequestMapping(value = "/potatoes", method = RequestMethod.GET)
    @ApiCallMetricAnnotation(name = "ListPotatos")
    public List<PotatoDTO> list(@RequestParam(value = "userId", required = false) UUID userId,
                                @RequestParam(value = "page", required = false, defaultValue = "0") Integer page,
                                @RequestParam(value = "size", required = false, defaultValue = "20") Integer size) {

        return potatoWebService.list(userId, page, size);
    }

    @LogDetail
    @RequestMapping(value = "/potatoes/search", method = RequestMethod.GET)
    @ApiCallMetricAnnotation(name = "SearchPotato")
    public List<PotatoDTO> search(@RequestParam(value = "userId", required = false) UUID userId,
                                  @RequestParam(value = "keyword", required = false, defaultValue = "") String keyword,
                                  @RequestParam(value = "page", required = false, defaultValue = "0") Integer page,
                                  @RequestParam(value = "size", required = false, defaultValue = "20") Integer size
    ) {
        return potatoWebService.search(userId, keyword, page, size);

    }

    @RequestMapping(value = "/ping", method = RequestMethod.GET)
    @ApiCallMetricAnnotation(name = "ping")
    public ServiceHealth ping() {
        return this.potatoWebService.ping();
    }
}
