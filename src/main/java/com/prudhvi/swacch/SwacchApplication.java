package com.prudhvi.swacch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SwacchApplication {

	public static void main(String[] args) {
		SpringApplication.run(SwacchApplication.class, args);
	}

}
