package com.github.walterfan.potato.common.config;

import com.github.walterfan.potato.common.metrics.elements.Application;
import com.github.walterfan.potato.common.metrics.elements.Environment;
import com.github.walterfan.potato.common.metrics.handler.MetricsHandler;
import com.github.walterfan.potato.common.metrics.handler.MetricsHandlerImpl;
import com.github.walterfan.potato.common.metrics.handler.output.MetricOutputerable;
import com.github.walterfan.potato.common.metrics.handler.output.OutputMetricByConsole;
import com.github.walterfan.potato.common.metrics.handler.output.OutputMetricByInfluxdb;
import com.github.walterfan.potato.common.metrics.handler.output.OutputMetricByLog;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;


@Configuration
@PropertySource("classpath:application.properties")
public class MetricsConfig {

    @Value("${spring.application.name:potato}")
    private String applicationName;

    @Value("${spring.application.component:potato-metrics}")
    private String component;

    @Value("${spring.application.version:1.0}")
    private String version;

    @Value("${spring.application.env:dev}")
    private String env;

    @Value("${spring.influxdb.url:http://localhost:8086}")
    private String influxdbUrl;

    @Value("${spring.influxdb.username:admin}")
    private String influxdbUserName;

    @Value("${spring.influxdb.password:admin}")
    private String influxdbPassword;

    @Value("${spring.influxdb.database:potato}")
    private String influxdbDatabase;

    @Bean
    public Application application() {
        return new Application(applicationName, component, version);
    }

    @Bean
    public Environment runEnv() throws UnknownHostException {
        return new Environment(env, InetAddress.getLocalHost().getHostName());
    }

    @Bean
    public MetricsHandler metricsHandler(Application application, Environment environment) {
        Collection<MetricOutputerable> metricOutputerables = newOutputs();
        return new MetricsHandlerImpl(application, environment, metricOutputerables);
    }

    private Collection<MetricOutputerable> newOutputs() {
        OutputMetricByConsole toConsoleOutputMetric = new OutputMetricByConsole();
        OutputMetricByLog toLogOutputMetric = new OutputMetricByLog();
        OutputMetricByInfluxdb outputMetricByInfluxdb = new OutputMetricByInfluxdb(influxdbUrl, influxdbUserName,
                influxdbPassword, influxdbDatabase);

        Collection<MetricOutputerable> metricOutputerables = new ArrayList<>();
        metricOutputerables.add(toConsoleOutputMetric);
        metricOutputerables.add(toLogOutputMetric);
        metricOutputerables.add(outputMetricByInfluxdb);

        return metricOutputerables;
    }

}