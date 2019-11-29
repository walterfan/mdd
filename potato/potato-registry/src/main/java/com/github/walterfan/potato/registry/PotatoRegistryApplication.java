package com.github.walterfan.potato.registry;

import de.codecentric.boot.admin.server.config.EnableAdminServer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@EnableEurekaServer
@SpringBootApplication
@EnableAdminServer
public class PotatoRegistryApplication {

	public static void main(String[] args) {
		SpringApplication.run(PotatoRegistryApplication.class, args);
	}

}
