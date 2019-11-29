package com.github.walterfan.potato.metrics;

import com.github.walterfan.potato.common.config.ZipkinConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
//import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.annotation.Import;
@EnableCircuitBreaker
@EnableDiscoveryClient
@SpringBootApplication
@Import(ZipkinConfig.class)
public class PotatoMetricsApplication {

	public static void main(String[] args) {
		SpringApplication.run(PotatoMetricsApplication.class, args);
	}

}
