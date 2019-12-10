package com.github.walterfan.potato.common.metrics;

import com.github.walterfan.potato.common.metrics.elements.event.ApiCallEvent;
import com.github.walterfan.potato.common.metrics.handler.MetricsHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;


@Component
@Order(Integer.MIN_VALUE)
@Slf4j
public class MetricsFilter implements Filter {

    @Autowired
    private MetricsHandler metricsHandler;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (!isHttpRequest(request)) {
            chain.doFilter(request, response);
            return;
        }
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        ApiCallEvent.Builder builder = new ApiCallEvent.Builder(httpServletRequest.getRequestURI(), httpServletRequest.getMethod(), UUID.randomUUID().toString());
        MetricThreadLocal.setCurrentMetricBuilder(builder);
        long startTime = System.currentTimeMillis();
        try {
            chain.doFilter(request, response);
        } catch (Exception e) {
            log.error("do filter error", e);
        } finally {
            ApiCallEvent apiCallEvent = buildApiEvent(response, builder, startTime);
            metricsHandler.handle(apiCallEvent);
        }

    }

    private boolean isHttpRequest(ServletRequest request) {
        return request instanceof HttpServletRequest;
    }

    private ApiCallEvent buildApiEvent(ServletResponse response, ApiCallEvent.Builder builder, long startTime) {
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;
        if (isSuccess(httpServletResponse)) {
            return builder.buildSuccessApiCall(System.currentTimeMillis() - startTime, httpServletResponse.getStatus());
        }

        return builder.buildFailedApiCall(System.currentTimeMillis() - startTime, httpServletResponse.getStatus(), String.valueOf(httpServletResponse.getStatus()));
    }

    public boolean isSuccess(HttpServletResponse httpServletResponse) {
        int status = httpServletResponse.getStatus();
        return status >= 200 && status < 400;
    }

    @Override
    public void destroy() {
    }

}