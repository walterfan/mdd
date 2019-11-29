package com.github.walterfan.potato.common.metrics;

import com.github.walterfan.potato.common.metrics.elements.event.ApiCallEvent;
import com.github.walterfan.potato.common.metrics.elements.event.PotatoMetricEvent;

public class MetricThreadLocal {

    private static final ThreadLocal<ApiCallEvent.Builder> METRIC_THREAD_LOCAL = new ThreadLocal<ApiCallEvent.Builder>();

    private static final ThreadLocal<PotatoMetricEvent> POTATO_METRIC_THREAD_LOCAL = new ThreadLocal<PotatoMetricEvent>();



    public static void setPotatoMetricEvent(PotatoMetricEvent event){
        POTATO_METRIC_THREAD_LOCAL.set(event);
    }


    public static PotatoMetricEvent getPotatoMetricEvent(){
        return POTATO_METRIC_THREAD_LOCAL.get();
    }



    public static void cleanPotatoMetricEvent(){
        POTATO_METRIC_THREAD_LOCAL.remove();
    }

    public static ApiCallEvent.Builder getCurrentMetricBuilder() {
        return METRIC_THREAD_LOCAL.get();
    }

    public static void setCurrentMetricBuilder(ApiCallEvent.Builder metricBuilder) {
        METRIC_THREAD_LOCAL.set(metricBuilder);
    }

}
