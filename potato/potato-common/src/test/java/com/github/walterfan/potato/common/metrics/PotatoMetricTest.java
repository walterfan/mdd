package com.github.walterfan.potato.common.metrics;


import com.github.walterfan.potato.common.metrics.elements.Application;
import com.github.walterfan.potato.common.metrics.elements.Environment;
import com.github.walterfan.potato.common.metrics.elements.event.PotatoMetricEvent;
import com.github.walterfan.potato.common.util.JsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.time.Instant;
import java.util.UUID;

@Slf4j
public class PotatoMetricTest {
    @Test
    public void testJson() {
        PotatoMetricEvent potatoMetricEvent = PotatoMetricEvent.builder()
                .name("create_potato")
                .trackingID(UUID.randomUUID().toString())
                .potatoId(UUID.randomUUID().toString())
                .potatoName("write blog of oauth2")
                .action(PotatoMetricEvent.Action.CREATE)
                .priority(1)
                .scheduledStartTime(Instant.parse("2019-09-09T09:00:00Z"))
                .scheduledEndTime(Instant.parse("2019-09-09T10:00:00Z"))
                .startTime(Instant.parse("2019-09-09T09:09:00Z"))
                .endTime(Instant.parse("2019-09-11T09:09:09Z"))
                .build();
        log.info("potatoMetricEvent: \n{}", JsonUtil.toJson(potatoMetricEvent));

        Application application = new Application("potato-service", "potato-server", "1.0.0");
        Environment environment  = new Environment("production", "10.224.77.99");
        Metrics metric = new Metrics(application, environment, potatoMetricEvent);
        log.info("potatoMetric: \n{}", JsonUtil.toJson(metric));
    }
}
