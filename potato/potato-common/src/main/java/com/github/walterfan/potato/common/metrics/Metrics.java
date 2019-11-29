package com.github.walterfan.potato.common.metrics;

import com.github.walterfan.potato.common.metrics.elements.Application;
import com.github.walterfan.potato.common.metrics.elements.Environment;
import com.github.walterfan.potato.common.metrics.elements.event.MetricEvent;


import lombok.Data;

@Data
public class Metrics {

    private Application application;
    private Environment environment;
    private MetricEvent event;

    public Metrics(Application application, Environment environment, MetricEvent event) {
        this.application = application;
        this.environment = environment;
        this.event = event;
    }

}
