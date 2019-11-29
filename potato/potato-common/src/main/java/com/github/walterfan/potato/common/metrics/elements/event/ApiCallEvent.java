package com.github.walterfan.potato.common.metrics.elements.event;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ApiCallEvent extends MetricEvent {

    private boolean success;
    private long responseCode;
    private long totalDurationInMS;
    private String failReason;
    private String endpoint;
    private String method;
    private List<Step> steps = new ArrayList();

    void addStep(Step step){
        this.steps.add(step);
    }

    private ApiCallEvent(String name, String trackingId) {
        super("interface", name, trackingId);
    }

    public static class Builder {

        private ApiCallEvent apiCallEvent;

        public Builder(String endpoint, String method, String trackingId) {
            this.apiCallEvent = new ApiCallEvent(String.format("%s %s", method, endpoint), trackingId);
            this.apiCallEvent.setEndpoint(endpoint);
            this.apiCallEvent.setMethod(method);
        }

        public Builder appendInformation(String key, Object value) {
            this.apiCallEvent.appendInformation(key, value);
            return this;
        }

        public void setName(String name) {
            this.apiCallEvent.setMetricName(name);
        }

        public void addStep(Step step){
            this.apiCallEvent.addStep(step);
        }

        public ApiCallEvent buildSuccessApiCall(long totalDurationInMS, long responseCode) {
            this.apiCallEvent.setSuccess(true);
            this.apiCallEvent.setResponseCode(responseCode);
            this.apiCallEvent.setTotalDurationInMS(totalDurationInMS);
            return apiCallEvent;
        }

        public ApiCallEvent buildFailedApiCall(long totalDurationInMS, long responseCode, String failReason) {
            this.apiCallEvent.setSuccess(false);
            this.apiCallEvent.setResponseCode(responseCode);
            this.apiCallEvent.setTotalDurationInMS(totalDurationInMS);
            this.apiCallEvent.setFailReason(failReason);
            return apiCallEvent;
        }

    }

}
