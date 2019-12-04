package com.github.walterfan.potato.common.metrics;


import com.github.walterfan.potato.common.metrics.elements.event.PotatoMetricEvent;
import com.github.walterfan.potato.common.util.JsonUtil;
import org.slf4j.MDC;

public class PotatoMetricHolder {
    public static final String POTATO_METRIC = "potato-metric";

    private PotatoMetricHolder() {
    }

    public static PotatoMetricEvent getPotatoMetric() {
        String str = MDC.get(POTATO_METRIC);
        return JsonUtil.fromJson(str, PotatoMetricEvent.class);
    }

    public static void setPotatoMetric(PotatoMetricEvent potatoMetric) {
        MDC.put(POTATO_METRIC, potatoMetric.toString());
    }

    public static void cleanPotatoMetric() {
        MDC.remove(POTATO_METRIC);
    }
}
