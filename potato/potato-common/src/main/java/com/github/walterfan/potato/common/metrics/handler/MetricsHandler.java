package com.github.walterfan.potato.common.metrics.handler;

import com.github.walterfan.potato.common.metrics.elements.event.MetricEvent;


public interface MetricsHandler {

    void handle(MetricEvent event);

}
