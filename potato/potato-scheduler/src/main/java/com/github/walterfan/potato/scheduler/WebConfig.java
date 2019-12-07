package com.github.walterfan.potato.scheduler;

import com.github.walterfan.potato.common.config.AbstractConfig;
import com.google.common.base.Predicates;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.Collections;



@Configuration
@EnableAutoConfiguration
@EnableSwagger2
@ComponentScan(basePackages = {
        "com.github.walterfan.potato.scheduler",
        "com.github.walterfan.potato.common"})
@PropertySource("classpath:application.properties")
public class WebConfig  extends AbstractConfig {


    @Override
    protected String getServiceName() {
        return "potato-scheduler";
    }
}