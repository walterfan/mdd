package com.github.walterfan.potato.registry;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@EnableEurekaServer
@SpringBootApplication
public class PotatoRegistryApplication {

	public static void main(String[] args) {
		SpringApplication.run(PotatoRegistryApplication.class, args);
	}

}
