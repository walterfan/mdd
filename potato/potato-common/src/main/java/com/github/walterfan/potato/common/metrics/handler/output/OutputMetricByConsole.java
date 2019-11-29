package com.github.walterfan.potato.common.metrics.handler.output;


import com.github.walterfan.potato.common.util.JsonUtil;
import com.github.walterfan.potato.common.metrics.Metrics;


public class OutputMetricByConsole extends AbstractOutputMetric {


    @Override
    public void output(Metrics metrics) {
        System.out.println(JsonUtil.toJson(metrics));
    }

}
