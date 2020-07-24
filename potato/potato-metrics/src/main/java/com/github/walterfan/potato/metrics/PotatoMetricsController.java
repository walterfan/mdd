package com.github.walterfan.potato.metrics;

import com.github.walterfan.potato.common.dto.PotatoDTO;
import com.github.walterfan.potato.common.dto.ServiceHealth;
import com.github.walterfan.potato.common.metrics.ApiCallMetricAnnotation;
import com.github.walterfan.potato.common.metrics.LogDetail;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
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
 * @Author: Walter Fan
 **/
@RestController
@RequestMapping("/potato/api/v1")
@Slf4j
public class PotatoMetricsController {



    @Autowired
    private ServiceHealthManager serviceHealthManager;


    @RequestMapping(value = "/ping", method = RequestMethod.GET)
    @ApiCallMetricAnnotation(name = "ping")
    public ServiceHealth ping() {
        return serviceHealthManager.checkHealth();
    }


}
