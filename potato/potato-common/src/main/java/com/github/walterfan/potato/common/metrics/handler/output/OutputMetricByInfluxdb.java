package com.github.walterfan.potato.common.metrics.handler.output;


import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import com.github.walterfan.potato.common.metrics.Metrics;
import com.github.walterfan.potato.common.metrics.elements.event.ApiCallEvent;
import com.github.walterfan.potato.common.metrics.elements.event.MetricEvent;
import com.github.walterfan.potato.common.metrics.elements.event.UsageEvent;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Point;
import org.influxdb.dto.Point.Builder;



import lombok.extern.slf4j.Slf4j;

@Slf4j
public class OutputMetricByInfluxdb extends AbstractOutputMetric {

    private InfluxDB influxDB;

    public OutputMetricByInfluxdb(String influxdbUrl, String userName, String password, String database) {

        try {
            this.influxDB = InfluxDBFactory.connect(influxdbUrl, userName, password);
            this.influxDB.setDatabase(database);
            this.influxDB.disableBatch();
            createDatabaseIfNeed(database);
        } catch(Exception e) {
            log.error("connect influxDB error: " + e.getMessage());
        }

    }

    @SuppressWarnings("deprecation")
    private void createDatabaseIfNeed(String database) {
        if (!this.influxDB.databaseExists(database)) {
            this.influxDB.createDatabase(database);
            log.info("created influxdb database: " + database);
        }
    }

    @Override
    public void output(Metrics metrics) {
        Builder measurement = Point.measurement(metrics.getApplication().getService());

        measurement.time(metrics.getEvent().getTimestamp(), TimeUnit.MILLISECONDS);

        measurement.tag("component", metrics.getApplication().getComponent());
        measurement.tag("version", metrics.getApplication().getVersion());

        measurement.tag("environment", metrics.getEnvironment().getName());
        measurement.tag("address", metrics.getEnvironment().getAddress());

        MetricEvent event = metrics.getEvent();
        measurement.tag("type", event.getMetricType());
        measurement.tag("name", event.getMetricName());

        measurement.addField("trackingId", event.getTrackingID());

        Map<String, Object> others = event.getOthers();
        Set<Entry<String, Object>> entrySet = others.entrySet();
        for (Entry<String, Object> entry : entrySet) {
            measurement.addField(entry.getKey(), String.valueOf(entry.getValue()));
        }

        if (event instanceof ApiCallEvent) {
            ApiCallEvent apiCallEvent = (ApiCallEvent) event;
            measurement.tag("success", String.valueOf(apiCallEvent.isSuccess()));
            measurement.tag("responseCode", String.valueOf(apiCallEvent.getResponseCode()));
            measurement.addField("totalDurationInMS", apiCallEvent.getTotalDurationInMS());
            measurement.addField("failReason", Optional.ofNullable(apiCallEvent.getFailReason()).map(x -> x).orElse(""));
        } else if (event instanceof UsageEvent) {
            UsageEvent usageEvent = (UsageEvent) event;
            Map<String, AtomicLong> statistics = usageEvent.getStatistics();
            for (Entry<String, AtomicLong> entry : statistics.entrySet()) {
                measurement.addField(entry.getKey(), entry.getValue().toString());
            }
        }

        Point point = measurement.build();
        if(influxDB != null) {
            try {
                influxDB.write(point);
            } catch(Exception e) {
                log.error("write influxDB error: " + e.getMessage());
            }
        }

    }


}
