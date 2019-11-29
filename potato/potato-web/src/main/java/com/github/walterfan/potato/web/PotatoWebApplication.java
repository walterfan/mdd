package com.github.walterfan.potato.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableCircuitBreaker
@EnableDiscoveryClient
public class PotatoWebApplication {

	public static void main(String[] args) {
		SpringApplication.run(PotatoWebApplication.class, args);
	}

}
