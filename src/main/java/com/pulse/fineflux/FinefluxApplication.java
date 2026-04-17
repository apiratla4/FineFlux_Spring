package com.pulse.fineflux;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

import java.io.File;
import java.time.ZoneId;
import java.util.TimeZone;

@SpringBootApplication
@EnableMongoAuditing
public class FinefluxApplication {

	public static void main(String[] args) {
		// Load .env file before Spring initializes
		loadEnvVariables();

		// Set default timezone to IST (Asia/Kolkata) for the entire application
		TimeZone.setDefault(TimeZone.getTimeZone(ZoneId.of("Asia/Kolkata")));
		System.setProperty("user.timezone", "Asia/Kolkata");

		SpringApplication.run(FinefluxApplication.class, args);
	}

	/**
	 * Load environment variables from .env file
	 */
	private static void loadEnvVariables() {
		String envPath = System.getProperty("user.dir");
		File envFile = new File(envPath, ".env");

		if (envFile.exists()) {
			try {
				Dotenv dotenv = Dotenv.configure()
						.directory(envPath)
						.load();

				// Map dotenv variables to system properties
				dotenv.entries().forEach(entry -> {
					String key = entry.getKey();
					String value = entry.getValue();
					if (System.getProperty(key) == null && value != null && !value.isEmpty()) {
						System.setProperty(key, value);
					}
				});
			} catch (Exception e) {
				System.err.println("Warning: Failed to load .env file: " + e.getMessage());
			}
		}
	}

}
