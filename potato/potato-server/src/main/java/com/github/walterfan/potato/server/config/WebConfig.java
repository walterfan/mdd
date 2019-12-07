package com.github.walterfan.potato.server.config;

import com.github.walterfan.potato.client.PotatoClientDemoApp;

import com.github.walterfan.potato.common.config.AbstractConfig;

import com.github.walterfan.potato.common.util.TemplateHelper;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;



import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import springfox.documentation.swagger2.annotations.EnableSwagger2;

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

}