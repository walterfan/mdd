package com.github.walterfan.potato.common.config;

import com.github.kristofa.brave.Brave;
import com.github.kristofa.brave.EmptySpanCollectorMetricsHandler;
import com.github.kristofa.brave.Sampler;
import com.github.kristofa.brave.SpanCollector;
import com.github.kristofa.brave.http.DefaultSpanNameProvider;
import com.github.kristofa.brave.http.HttpSpanCollector;
import com.github.kristofa.brave.servlet.BraveServletFilter;
import com.github.kristofa.brave.spring.BraveClientHttpRequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;

@Configuration
public class ZipkinConfig {

    @Value("${spring.application.name: potato}")
    private String serviceName;

    @Value("${spring.zipkin.url:http://localhost:9411}")
    private String zipKinUrl;

    @Bean
    public SpanCollector spanCollector() {
        HttpSpanCollector.Config config = HttpSpanCollector.Config.builder()
                .compressionEnabled(false)
                .connectTimeout(2000)
                .flushInterval(1)
                .readTimeout(2000)
                .build();
        return HttpSpanCollector.create(zipKinUrl, config, new EmptySpanCollectorMetricsHandler());
    }

    @Bean
    public Brave brave(SpanCollector spanCollector) {
        Brave.Builder builder = new Brave.Builder(serviceName);
        builder.spanCollector(spanCollector);
        builder.traceSampler(Sampler.create(1));
        return builder.build();
    }

    @Bean
    public BraveServletFilter braveServletFilter(Brave brave) {
        LoggableBraveServletFilter filter = new LoggableBraveServletFilter(brave.serverRequestInterceptor(),
                brave.serverResponseInterceptor(), new DefaultSpanNameProvider());
        return filter;
    }

    @Bean
    public BraveClientHttpRequestInterceptor braveClientHttpRequestInterceptor(Brave brave) {
        BraveClientHttpRequestInterceptor interceptor = new BraveClientHttpRequestInterceptor(brave.clientRequestInterceptor(),
                brave.clientResponseInterceptor(), new DefaultSpanNameProvider());
        return interceptor;
    }

    @Bean
    public RestTemplateCustomizer restTemplateCustomizerForBrave(BraveClientHttpRequestInterceptor braveClientHttpRequestInterceptor) {
       return new RestTemplateCustomizer() {
           @Override
           public void customize(RestTemplate restTemplate) {
               restTemplate.setInterceptors(Arrays.asList(braveClientHttpRequestInterceptor));

           }
       };
    }


}