package com.IKov.TwittService;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class TwittServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(TwittServiceApplication.class, args);
	}

}
