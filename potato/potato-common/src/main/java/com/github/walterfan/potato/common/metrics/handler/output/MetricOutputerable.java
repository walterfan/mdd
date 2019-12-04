package com.github.walterfan.potato.common.metrics.handler.output;


import com.github.walterfan.potato.common.metrics.Metrics;

public interface MetricOutputerable {

    boolean isNeedOutput(Metrics metrics);

    void output(Metrics metrics);

}
