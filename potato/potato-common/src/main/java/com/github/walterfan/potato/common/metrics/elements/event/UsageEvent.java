package com.github.walterfan.potato.common.metrics.elements.event;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import lombok.Data;

@Data
public class UsageEvent extends MetricEvent {

    private Map<String, AtomicLong> statistics = new LinkedHashMap<String, AtomicLong>(10);

    public UsageEvent(String name, String trackingID) {
        super("usage", name, trackingID);
    }

    public void setStatisticItem(String item, long number) {
        statistics.putIfAbsent(item, new AtomicLong());
        statistics.get(item).set(number);
    }

}
