package com.checkmate.bub;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class BubApplication {

	public static void main(String[] args) {
		SpringApplication.run(BubApplication.class, args);
	}

}
