package com.github.walterfan.potato.common.config;

import com.github.kristofa.brave.ServerRequestInterceptor;
import com.github.kristofa.brave.ServerResponseInterceptor;
import com.github.kristofa.brave.http.SpanNameProvider;
import com.github.kristofa.brave.servlet.BraveServletFilter;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;

@Slf4j
public class LoggableBraveServletFilter  extends BraveServletFilter {
    public LoggableBraveServletFilter(ServerRequestInterceptor requestInterceptor, ServerResponseInterceptor responseInterceptor, SpanNameProvider spanNameProvider) {
        super(requestInterceptor, responseInterceptor, spanNameProvider);
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {
        log.info("zipkin filter: " + request);
        super.doFilter(request, response, filterChain);
    }
}
