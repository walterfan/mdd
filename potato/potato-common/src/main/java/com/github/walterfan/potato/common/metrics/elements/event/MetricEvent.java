package com.github.walterfan.potato.common.metrics.elements.event;

import java.util.LinkedHashMap;
import java.util.Map;

import lombok.Data;

@Data
public abstract class MetricEvent {

    protected String metricType;
    protected String metricName;
    protected String trackingID;
    protected long timestamp;

    private Map<String, Object> others = new LinkedHashMap<String, Object>(10);

    protected MetricEvent(String type, String name, String trackingID) {
        this.metricType = type;
        this.metricName = name;
        this.trackingID = trackingID;
        this.timestamp = System.currentTimeMillis();
    }

    public void appendInformation(String key, Object value) {
        this.others.put(key, value);
    }

}