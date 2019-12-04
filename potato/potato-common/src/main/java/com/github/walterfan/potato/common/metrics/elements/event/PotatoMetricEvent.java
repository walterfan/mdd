package com.github.walterfan.potato.common.metrics.elements.event;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;



@Getter
@Setter
public class PotatoMetricEvent extends MetricEvent {
    public enum Action {
        CREATE,
        UPDATE,
        DELETE,
        START,
        END
    }


    private PotatoMetricEvent(String type, String name, String trackingID, Action action, String potatoId, String potatoName, String tags, int priority, Instant startTime, Instant endTime, Instant scheduledStartTime, Instant scheduledEndTime) {
        super(type, name, trackingID);
        this.action = action;
        this.potatoId = potatoId;
        this.potatoName = potatoName;
        this.tags = tags;
        this.priority = priority;
        this.startTime = startTime;
        this.endTime = endTime;
        this.scheduledStartTime = scheduledStartTime;
        this.scheduledEndTime = scheduledEndTime;
    }

    private Action action;
    private String potatoId;
    private String potatoName;
    private String tags;
    private int priority;
    private Instant startTime;
    private Instant endTime;
    private Instant scheduledStartTime;
    private Instant scheduledEndTime;

    public static Builder builder() {
        return new Builder();
    }
    public static class Builder {
        protected String name;
        protected String trackingID;
        private Action action;
        private String potatoId;
        private String potatoName;
        private String tags;
        private int priority;
        private Instant startTime;
        private Instant endTime;
        private Instant scheduledStartTime;
        private Instant scheduledEndTime;

        public Builder action(Action action) {
            this.action = action;
            return this;
        }

        public Builder potatoId(String potatoId) {
            this.potatoId = potatoId;
            return this;
        }

        public Builder potatoName(String potatoName) {
            this.potatoName = potatoName;
            return this;
        }

        public Builder tags(String tags) {
            this.tags = tags;
            return this;
        }

        public Builder priority(int priority) {
            this.priority = priority;
            return this;
        }

        public Builder startTime(Instant startTime) {
            this.startTime = startTime;
            return this;
        }

        public Builder endTime(Instant endTime) {
            this.endTime = endTime;
            return this;
        }

        public Builder scheduledStartTime(Instant scheduledStartTime) {
            this.scheduledStartTime = scheduledStartTime;
            return this;
        }

        public Builder scheduledEndTime(Instant scheduledEndTime) {
            this.scheduledEndTime = scheduledEndTime;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder trackingID(String trackingID) {
            this.trackingID = trackingID;
            return this;
        }

        public PotatoMetricEvent build() {
            return new PotatoMetricEvent("potato", this.name, this.trackingID, this.action,
                    this.potatoId, this.potatoName, this.tags, this.priority,
                    this.startTime, this.endTime, this.scheduledStartTime, this.scheduledEndTime);
        }
    }
}
