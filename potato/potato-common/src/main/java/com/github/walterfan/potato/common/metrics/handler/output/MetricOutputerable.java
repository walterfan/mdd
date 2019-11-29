package com.github.walterfan.potato.common.metrics.handler.output;


import com.github.walterfan.potato.common.metrics.Metrics;

public interface MetricOutputerable {

    public boolean isNeedOutput(Metrics metrics);

    public void output(Metrics metrics);

}
