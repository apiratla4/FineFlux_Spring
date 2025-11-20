package com.pulse.fineflux;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

import java.time.ZoneId;
import java.util.TimeZone;

@SpringBootApplication
@EnableMongoAuditing
public class FinefluxApplication {

	public static void main(String[] args) {
		// Set default timezone to IST (Asia/Kolkata) for the entire application
		TimeZone.setDefault(TimeZone.getTimeZone(ZoneId.of("Asia/Kolkata")));
		System.setProperty("user.timezone", "Asia/Kolkata");

		SpringApplication.run(FinefluxApplication.class, args);
	}

}
