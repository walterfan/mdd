package com.github.walterfan.potato.web;


import com.github.walterfan.potato.common.dto.PotatoDTO;
import com.github.walterfan.potato.common.dto.ServiceHealth;
import com.github.walterfan.potato.common.metrics.ApiCallMetricAnnotation;
import com.github.walterfan.potato.common.metrics.LogDetail;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;


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
