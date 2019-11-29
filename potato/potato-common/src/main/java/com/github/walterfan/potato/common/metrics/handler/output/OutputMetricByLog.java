package com.github.walterfan.potato.common.metrics.handler.output;


import com.github.walterfan.potato.common.metrics.Metrics;
import com.github.walterfan.potato.common.util.JsonUtil;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class OutputMetricByLog extends AbstractOutputMetric {

    @Override
    public void output(Metrics metrics) {
        log.info(JsonUtil.toJson(metrics));
    }

}
