package com.github.walterfan.potato.server.config;

import com.github.walterfan.potato.client.PotatoClientDemoApp;

import com.github.walterfan.potato.common.config.AbstractConfig;

import com.github.walterfan.potato.common.util.TemplateHelper;

import com.google.common.base.Predicate;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;



import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import springfox.documentation.RequestHandler;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.Collections;

@Configuration
@EnableAutoConfiguration
@EnableSwagger2
@EnableJpaRepositories(basePackages = {"com.github.walterfan.potato.server"})
@Import({
        TemplateHelper.class
})
@ComponentScan(basePackages = {
        "com.github.walterfan.potato.server",
        "com.github.walterfan.potato.client",
        "com.github.walterfan.potato.common"
        },
        excludeFilters={
            @ComponentScan.Filter(type=FilterType.ASSIGNABLE_TYPE, value=PotatoClientDemoApp.class)
        })

@PropertySource("classpath:application.properties")
public class WebConfig extends AbstractConfig {

    @Override
    protected String getServiceName() {
        return "potato-service";
    }

    protected Predicate<RequestHandler> getApiSelector() {
        return RequestHandlerSelectors.basePackage("com.github.walterfan.potato.server");
    }

    protected ApiInfo apiInfo() {
        return new ApiInfo(
                "Potato Service REST API",
                "API for Potato(TODO List) Service.",
                "1.0",
                "Terms of service",
                new Contact("Walter Fan", "http://www.fanyamin.com", "walter.fan@gmail.com"),
                "License of API", "API license URL", Collections.emptyList());
    }
}