package com.github.walterfan.potato.dashboard;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;

@EnableConfigServer
@SpringBootApplication
public class PotatoConfigApplication {

	public static void main(String[] args) {
		SpringApplication.run(PotatoConfigApplication.class, args);
	}

}
