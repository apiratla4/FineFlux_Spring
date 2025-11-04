package com.pulse.fineflux;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

@SpringBootApplication
@EnableMongoAuditing
public class FinefluxApplication {

	public static void main(String[] args) {
		SpringApplication.run(FinefluxApplication.class, args);
	}

}
