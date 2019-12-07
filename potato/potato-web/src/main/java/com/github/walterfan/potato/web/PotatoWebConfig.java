package com.github.walterfan.potato.web;



import com.github.walterfan.potato.client.PotatoClientDemoApp;
import com.github.walterfan.potato.common.config.AbstractConfig;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.*;

import springfox.documentation.swagger2.annotations.EnableSwagger2;





@Configuration
@EnableAutoConfiguration
@EnableSwagger2
@ComponentScan(basePackages = {
        "com.github.walterfan.potato.client",
        "com.github.walterfan.potato.common"
},
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = PotatoClientDemoApp.class)
        })

@PropertySource("classpath:application.properties")

public class PotatoWebConfig extends AbstractConfig {

        @Override
        protected String getServiceName() {
                return "potato-web";
        }
}