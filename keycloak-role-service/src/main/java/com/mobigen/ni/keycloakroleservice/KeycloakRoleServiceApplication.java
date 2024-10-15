package com.mobigen.ni.keycloakroleservice;

import lombok.extern.slf4j.Slf4j;
import org.keycloak.adapters.springboot.KeycloakSpringBootConfigResolver;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

import java.util.Date;
import java.util.TimeZone;

@Slf4j
@ComponentScan(basePackages = {"com"})
@SpringBootApplication
public class KeycloakRoleServiceApplication {

	public static void main(String[] args) {
		TimeZone.setDefault(TimeZone.getTimeZone("Asia/Seoul"));
		SpringApplication.run(KeycloakRoleServiceApplication.class, args);
		log.info("#################################################################");
		log.info("PROGRAM START: CURRENT TIME: " + new Date());
		log.info("#################################################################");
	}

	@Bean
	public KeycloakSpringBootConfigResolver keycloakConfigResolver() {
		return new KeycloakSpringBootConfigResolver();
	}

}
