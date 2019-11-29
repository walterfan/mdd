package com.github.walterfan.potato.common.metrics.handler;

import java.util.Collection;

import com.github.walterfan.potato.common.metrics.Metrics;
import com.github.walterfan.potato.common.metrics.elements.Application;
import com.github.walterfan.potato.common.metrics.elements.Environment;
import com.github.walterfan.potato.common.metrics.handler.output.MetricOutputerable;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MetricsHandlerImpl extends AbstractMetricsHandler {

    private final Collection<MetricOutputerable> metricOutputerables;

    public MetricsHandlerImpl(Application application, Environment environment, Collection<MetricOutputerable> metricOutputerables) {
        super(application, environment);
        this.metricOutputerables = metricOutputerables;
    }


    @Override
    protected void output(Metrics metrics) {
        for (MetricOutputerable metricOutputerable : metricOutputerables) {
            try {
                if (metricOutputerable.isNeedOutput(metrics)) {
                    metricOutputerable.output(metrics);
                }
            } catch (Exception e) {
                log.error("write metric fail for: " + e.getMessage(), e);
            }
        }
    }

}
