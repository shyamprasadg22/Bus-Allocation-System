package com.college.busallocation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

@SpringBootApplication
public class BusAllocationApplication {

    public static void main(String[] args) {
        loadDotEnvIfPresent();
        SpringApplication.run(BusAllocationApplication.class, args);
    }

    /**
     * Loads .env key-value pairs into System properties if present and not already set in OS env.
     */
    private static void loadDotEnvIfPresent() {
        File envFile = new File(".env");
        if (envFile.exists() && envFile.isFile()) {
            Properties props = new Properties();
            try (FileInputStream fis = new FileInputStream(envFile)) {
                props.load(fis);
                props.forEach((k, v) -> {
                    String key = k.toString().trim();
                    String val = v.toString().trim();
                    // Strip quotes if present
                    if ((val.startsWith("\"") && val.endsWith("\"")) || (val.startsWith("'") && val.endsWith("'"))) {
                        val = val.substring(1, val.length() - 1);
                    }
                    if (System.getenv(key) == null && System.getProperty(key) == null) {
                        System.setProperty(key, val);
                    }
                });
            } catch (IOException e) {
                System.err.println("Notice: Could not load .env file: " + e.getMessage());
            }
        }
    }
}
