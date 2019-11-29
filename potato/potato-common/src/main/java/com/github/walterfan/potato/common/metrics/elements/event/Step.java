package com.github.walterfan.potato.common.metrics.elements.event;

import lombok.Data;

@Data
public class Step {
    private String componentName;
    private String stepName;
    private long totalDurationInMS;
}
