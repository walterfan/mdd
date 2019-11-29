package com.github.walterfan.potato.common.metrics.handler;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


import com.github.walterfan.potato.common.metrics.Metrics;
import com.github.walterfan.potato.common.metrics.elements.Application;
import com.github.walterfan.potato.common.metrics.elements.Environment;
import com.github.walterfan.potato.common.metrics.elements.event.MetricEvent;
import lombok.extern.slf4j.Slf4j;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

@Slf4j
public abstract class AbstractMetricsHandler implements MetricsHandler {

    private static final ExecutorService WRITE_METRIC_THREAD = getMetricThread();

    private static ThreadPoolExecutor getMetricThread() {
        ThreadFactory threadFactory = new ThreadFactoryBuilder().setDaemon(true).setNameFormat("metric-thread-%d").build();
        return new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>(100_000), threadFactory, new ThreadPoolExecutor.CallerRunsPolicy());
    }

    private Application application;
    private Environment environment;

    protected AbstractMetricsHandler(Application application, Environment environment) {
        this.application = application;
        this.environment = environment;
    }

    @Override
    public void handle(MetricEvent event) {
        WRITE_METRIC_THREAD.submit(() -> {

            Metrics metric = convertFromEventToMetrics(event);
            try {
                output(metric);
            } catch (Exception e) {
                log.error("write metric fail for: " + e.getMessage(), e);
            }

        });

    }

    private Metrics convertFromEventToMetrics(MetricEvent event) {
        return new Metrics(application, environment, event);
    }

    protected abstract void output(Metrics metric);

    public static void shutdown() {
        WRITE_METRIC_THREAD.shutdownNow();
    }

}
