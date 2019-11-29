package com.github.walterfan.potato.common.metrics;

import com.github.walterfan.potato.common.metrics.elements.event.ApiCallEvent;
import com.github.walterfan.potato.common.metrics.elements.event.Step;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.StopWatch;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;


@Component
@Aspect
@Slf4j
public class MetricsAspect {

    @Around("@annotation(apiCallMetricAnnotation)")
    public Object aroundApiCall(ProceedingJoinPoint pjp, ApiCallMetricAnnotation apiCallMetricAnnotation) throws Throwable {
        String name = apiCallMetricAnnotation.name();
        ApiCallEvent.Builder builder = MetricThreadLocal.getCurrentMetricBuilder();
        if (builder != null) {
            builder.setName(name);
        }
        return pjp.proceed();
    }

    @Around("@annotation(clientCallMetricAnnotation)")
    public Object aroundClientCall(ProceedingJoinPoint pjp, ClientCallMetricAnnotation clientCallMetricAnnotation) throws Throwable {
        String name = clientCallMetricAnnotation.name();
        String component = clientCallMetricAnnotation.component();
        Step step = new Step();
        step.setStepName(name);
        step.setComponentName(component);
        log.info("-- aroundClientCall step: {}", step);
        StopWatch stopWatch = StopWatch.createStarted();
        try {
            return pjp.proceed();
        }finally {
            stopWatch.stop();
            step.setTotalDurationInMS(stopWatch.getTime(TimeUnit.MILLISECONDS));
            ApiCallEvent.Builder builder = MetricThreadLocal.getCurrentMetricBuilder();
            if(builder != null) {
                builder.addStep(step);
            }
        }
    }

}
