package com.github.walterfan.potato.common.config;

import com.codahale.metrics.InstrumentedExecutorService;
import com.codahale.metrics.MetricRegistry;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import springfox.documentation.RequestHandler;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.Collections;
import java.util.concurrent.*;

import static springfox.documentation.builders.PathSelectors.regex;

@EnableSwagger2
@Slf4j
public abstract class AbstractConfig {
    protected boolean enableSwagger = true;

    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
                .forCodeGeneration(Boolean.TRUE)
                .select()
                .apis(getApiSelector())
                //.paths(regex("/potato/api/v1/*"))
                //.paths(Predicates.and(regex("/api.*")))
                .paths(PathSelectors.any())
                .build()
                .enable(enableSwagger)
                .apiInfo(apiInfo());
    }

    protected Predicate<RequestHandler> getApiSelector() {
        return RequestHandlerSelectors.basePackage("com.github.walterfan.potato");
    }

    protected ApiInfo apiInfo() {
        return new ApiInfo(
                "REST API",
                "REST description of API.",
                "1.0",
                "Terms of service",
                new Contact("Walter Fan", "http://www.fanyamin.com", "walter.fan@gmail.com"),
                "License of API", "API license URL", Collections.emptyList());
    }

    @Lazy
    @Bean
    public MetricRegistry metricRegistry() {
        return new MetricRegistry();
    }

    @Bean
    public InstrumentedExecutorService instrumentedHealthCheckExecutor() {
        return new InstrumentedExecutorService(healthCheckExecutorService(), metricRegistry(), getServiceName() );
    }

    @Bean
    public ExecutorService healthCheckExecutorService() {
        int minThread = 2;
        int maxThread = 4;
        int keepalive = 60;
        int queueSize = 20;

        RejectedExecutionHandler handler = new RejectedExecutionHandler() {
            @Override
            public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
                log.warn("executor dropped metric.");
            }
        };
        BlockingQueue<Runnable> queue = new LinkedBlockingQueue<Runnable>(queueSize);
        ThreadPoolExecutor executor = new ThreadPoolExecutor(minThread,
                maxThread,
                keepalive,
                TimeUnit.SECONDS,
                queue,
                handler);
        //executor.setThreadN
        return executor;
    }

    protected abstract String getServiceName();
}
