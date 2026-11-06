package com.college.busallocation.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class DotenvEnvironmentPostProcessor implements EnvironmentPostProcessor {

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        File envFile = new File(".env");
        if (!envFile.exists() || !envFile.isFile()) {
            return;
        }

        Map<String, Object> envProperties = new HashMap<>();
        Properties props = new Properties();

        try (FileInputStream fis = new FileInputStream(envFile)) {
            props.load(fis);
            props.forEach((k, v) -> {
                String key = k.toString().trim();
                String val = v.toString().trim();
                if ((val.startsWith("\"") && val.endsWith("\"")) || (val.startsWith("'") && val.endsWith("'"))) {
                    val = val.substring(1, val.length() - 1);
                }
                envProperties.put(key, val);
                // Also set system property if absent
                if (System.getProperty(key) == null) {
                    System.setProperty(key, val);
                }
            });

            environment.getPropertySources().addFirst(new MapPropertySource("dotenvProperties", envProperties));
        } catch (IOException e) {
            System.err.println("Notice: Could not load .env file in postProcessEnvironment: " + e.getMessage());
        }
    }
}
