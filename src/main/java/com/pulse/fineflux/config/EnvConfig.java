package com.pulse.fineflux.config;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.context.annotation.Configuration;

import java.io.File;

@Configuration
public class EnvConfig {

    static {
        // Load .env file if it exists
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
                    if (System.getProperty(key) == null) {
                        System.setProperty(key, value);
                    }
                });
            } catch (Exception e) {
                System.err.println("Warning: Failed to load .env file: " + e.getMessage());
            }
        }
    }
}

