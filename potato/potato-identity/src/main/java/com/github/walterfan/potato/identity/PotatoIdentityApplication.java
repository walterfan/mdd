package com.github.walterfan.potato.identity;

import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableDiscoveryClient
public class PotatoIdentityApplication {

	@Value("${spring.datasource.url: jdbc:mysql://localhost/userservice?useUnicode=true&characterEncoding=utf8}")
	private String url;

	@Value("${spring.datasource.username: walter}")
	private String username;

	@Value("${spring.datasource.password: pass1234}")
	private String password;

	@Value("${connect.database: userservice}")
	private String database;

	@Bean(initMethod = "migrate")
	public Flyway flyway() {
		String urlWithoutDbName = url.substring(0, url.lastIndexOf("/"));
		Flyway flyway = new Flyway();
		flyway.setDataSource(urlWithoutDbName, username, password);
		flyway.setSchemas(database);
		flyway.setBaselineOnMigrate(true);
		return flyway;
	}
	public static void main(String[] args) {
		SpringApplication.run(PotatoIdentityApplication.class, args);
	}

}
