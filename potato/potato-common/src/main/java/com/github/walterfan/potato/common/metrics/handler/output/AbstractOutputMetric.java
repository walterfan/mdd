package com.github.walterfan.potato.common.metrics.handler.output;

import com.github.walterfan.potato.common.metrics.Metrics;

public abstract class AbstractOutputMetric implements MetricOutputerable {

    @Override
    public boolean isNeedOutput(Metrics metrics) {
        return true;
    }

}
